package com.meetory.message.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meetory.common.exception.CustomException;
import com.meetory.common.exception.ErrorCode;
import com.meetory.message.dto.InboxResponse;
import com.meetory.message.dto.InquiryRequest;
import com.meetory.message.dto.MessageItemResponse;
import com.meetory.message.dto.ReplyRequest;
import com.meetory.message.dto.ThreadDetailResponse;
import com.meetory.message.dto.ThreadSummaryResponse;
import com.meetory.message.entity.MessageItem;
import com.meetory.message.entity.MessageThread;
import com.meetory.message.repository.MessageItemRepository;
import com.meetory.message.repository.MessageThreadRepository;
import com.meetory.member.entity.Member;
import com.meetory.member.entity.MemberStatus;
import com.meetory.member.repository.MemberRepository;
import com.meetory.team.entity.Team;
import com.meetory.team.repository.TeamRepository;

import com.meetory.user.entity.User;
import com.meetory.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
// 같은 모임(team) + 같은 두 사람이면 대화방(MessageThread)을 재사용해서
// 카카오톡/인스타 DM 처럼 답장이 하나의 스레드에 계속 쌓이도록 한다.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private static final int PREVIEW_LENGTH = 40;

    private final MessageThreadRepository threadRepository;
    private final MessageItemRepository itemRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    // 팀 매칭 화면 "문의하기" 버튼 -> 최초 쪽지 전송 (이미 대화방이 있으면 이어서 전송)
    @Transactional
    public Long sendInquiry(Long teamId, Long senderId, InquiryRequest request) {
        Team team = findTeam(teamId);
        User leader = team.getLeader();

        if (leader.getId().equals(senderId)) {
            throw new CustomException(ErrorCode.SELF_INQUIRY_NOT_ALLOWED);
        }

        User sender = findUser(senderId);

        MessageThread thread = threadRepository
                .findByTeamIdAndStarterIdAndLeaderId(teamId, senderId, leader.getId())
                .orElseGet(() -> threadRepository.save(
                        MessageThread.builder()
                                .team(team)
                                .starter(sender)
                                .leader(leader)
                                .title(request.title())
                                .build()));

        saveItem(thread, sender, request.content());
        return thread.getId();
    }

    // 채팅형 팝업 하단 입력창 -> 전송 버튼 (문의를 보낸 사람, 받은 사람 모두 동일 로직 사용)
    @Transactional
    public Long reply(Long threadId, Long senderId, ReplyRequest request) {
        MessageThread thread = findThread(threadId);
        requireParticipant(thread, senderId);

        User sender = findUser(senderId);
        MessageItem item = saveItem(thread, sender, request.content());
        return item.getId();
    }

    // 마이페이지 "받은 쪽지" 패널 - 안읽음/읽음 목록
    public InboxResponse getInbox(Long userId) {
        List<MessageThread> threads = threadRepository.findAllByParticipant(userId);

        List<ThreadSummaryResponse> unread = new ArrayList<>();
        List<ThreadSummaryResponse> read = new ArrayList<>();

        for (MessageThread thread : threads) {
            boolean hasUnread = itemRepository.countByThreadIdAndSenderIdNotAndReadFalse(thread.getId(), userId) > 0;
            ThreadSummaryResponse summary = toSummary(thread, userId, hasUnread);

            if (hasUnread) {
                unread.add(summary);
            } else {
                read.add(summary);
            }
        }

        return new InboxResponse(unread, read);
    }

    // 쪽지 클릭 -> 채팅형 팝업. 조회와 동시에 상대가 보낸 안읽은 메시지를 읽음 처리한다.
    @Transactional
    public ThreadDetailResponse getThreadDetail(Long threadId, Long userId) {
        MessageThread thread = findThread(threadId);
        requireParticipant(thread, userId);

        List<MessageItem> unreadForMe = itemRepository.findByThreadIdAndSenderIdNotAndReadFalse(threadId, userId);
        unreadForMe.forEach(MessageItem::markRead);

        List<MessageItemResponse> messages = itemRepository.findByThreadIdOrderByCreatedAtAsc(threadId).stream()
                .map(m -> new MessageItemResponse(
                        m.getId(),
                        m.getSender().getId(),
                        m.getSender().getNickname(),
                        m.getContent(),
                        m.getSender().getId().equals(userId),
                        m.getCreatedAt()
                ))
                .collect(Collectors.toList());

        User other = thread.theOther(userId);

        return new ThreadDetailResponse(
                thread.getId(),
                thread.resolvedTeamId(),
                thread.resolvedTeamTitle(),
                thread.getTitle(),
                other.getId(),
                other.getNickname(),
                messages
        );
    }

    // 리더가 모임을 삭제할 때 승인된 일반 멤버에게 탈퇴 안내 쪽지를 보낸다.
    @Transactional
    public void notifyTeamDissolved(Team team) {
        User leader = team.getLeader();
        String warningTitle = "[모임 삭제] " + team.getTitle();
        String warningContent =
                "참여 중이던 '" + team.getTitle() + "' 모임이 리더에 의해 삭제되어 자동으로 탈퇴되었습니다.";

        memberRepository.findByTeamIdAndStatus(team.getId(), MemberStatus.승인).stream()
                .map(Member::getUser)
                .filter(user -> !user.getId().equals(leader.getId()))
                .forEach(member -> sendDissolveNotice(team, leader, member, warningTitle, warningContent));
    }

    @Transactional
    public void detachThreadsFromTeam(Long teamId, String teamTitle) {
        for (MessageThread thread : threadRepository.findByTeamId(teamId)) {
            thread.detachFromTeam(teamTitle);
            threadRepository.save(thread);
        }
    }

    @Transactional
    public void deleteThreadsByUserId(Long userId) {
        for (MessageThread thread : threadRepository.findByStarterId(userId)) {
            itemRepository.deleteByThreadId(thread.getId());
            threadRepository.delete(thread);
        }
        for (MessageThread thread : threadRepository.findByLeaderId(userId)) {
            itemRepository.deleteByThreadId(thread.getId());
            threadRepository.delete(thread);
        }
    }

    private void sendDissolveNotice(Team team, User leader, User member, String title, String content) {
        MessageThread thread = threadRepository
                .findByTeamIdAndStarterIdAndLeaderId(team.getId(), member.getId(), leader.getId())
                .orElseGet(() -> threadRepository.save(
                        MessageThread.builder()
                                .team(team)
                                .starter(member)
                                .leader(leader)
                                .title(title)
                                .build()));
        saveItem(thread, leader, content);
    }

    private MessageItem saveItem(MessageThread thread, User sender, String content) {
        MessageItem item = MessageItem.builder()
                .thread(thread)
                .sender(sender)
                .content(content)
                .build();
        MessageItem saved = itemRepository.save(item);
        thread.touch();
        return saved;
    }

    private ThreadSummaryResponse toSummary(MessageThread thread, Long userId, boolean hasUnread) {
        MessageItem last = itemRepository.findTopByThreadIdOrderByCreatedAtDesc(thread.getId()).orElse(null);

        return new ThreadSummaryResponse(
                thread.getId(),
                thread.resolvedTeamId(),
                thread.resolvedTeamTitle(),
                thread.getTitle(),
                thread.theOther(userId).getNickname(),
                truncate(last == null ? "" : last.getContent()),
                thread.getLastMessageAt(),
                hasUnread
        );
    }

    private void requireParticipant(MessageThread thread, Long userId) {
        if (!thread.isParticipant(userId)) {
            throw new CustomException(ErrorCode.NOT_THREAD_PARTICIPANT);
        }
    }

    private String truncate(String content) {
        if (content == null) {
            return "";
        }
        return content.length() > PREVIEW_LENGTH ? content.substring(0, PREVIEW_LENGTH) + "..." : content;
    }

    private MessageThread findThread(Long threadId) {
        return threadRepository.findById(threadId)
                .orElseThrow(() -> new CustomException(ErrorCode.THREAD_NOT_FOUND));
    }

    private Team findTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
