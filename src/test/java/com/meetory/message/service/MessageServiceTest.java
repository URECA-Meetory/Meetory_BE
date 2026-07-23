package com.meetory.message.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.meetory.common.exception.CustomException;
import com.meetory.message.dto.InboxResponse;
import com.meetory.message.dto.InquiryRequest;
import com.meetory.message.dto.ReplyRequest;
import com.meetory.message.dto.ThreadDetailResponse;
import com.meetory.message.entity.MessageItem;
import com.meetory.message.entity.MessageThread;
import com.meetory.message.repository.MessageItemRepository;
import com.meetory.message.repository.MessageThreadRepository;
import com.meetory.team.entity.Team;
import com.meetory.team.entity.TeamCategory;
import com.meetory.team.repository.TeamRepository;
import com.meetory.user.entity.Role;
import com.meetory.user.entity.User;
import com.meetory.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageThreadRepository threadRepository;

    @Mock
    private MessageItemRepository itemRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

    private User buildUser(Long id, String email, String nickname) {
        User user = User.builder()
                .email(email)
                .password("encoded")
                .nickname(nickname)
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Team buildTeam(Long id, User leader) {
        Team team = Team.builder()
                .title("알고리즘 스터디")
                .category(TeamCategory.스터디)
                .description("설명")
                .maxMembers(5)
                .leader(leader)
                .build();
        ReflectionTestUtils.setField(team, "id", id);
        return team;
    }

    private MessageThread buildThread(Long id, Team team, User starter, User leader) {
        MessageThread thread = MessageThread.builder()
                .team(team)
                .starter(starter)
                .leader(leader)
                .title("문의 제목")
                .build();
        ReflectionTestUtils.setField(thread, "id", id);
        ReflectionTestUtils.invokeMethod(thread, "prePersist");
        return thread;
    }

    @Test
    void 문의보내기_성공_새대화방생성() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        User applicant = buildUser(2L, "member@test.com", "지원자");
        Team team = buildTeam(100L, leader);
        InquiryRequest request = new InquiryRequest("궁금해요", "정원이 몇 명인가요?");

        given(teamRepository.findById(100L)).willReturn(Optional.of(team));
        given(userRepository.findById(2L)).willReturn(Optional.of(applicant));
        given(threadRepository.findByTeamIdAndStarterIdAndLeaderId(100L, 2L, 1L)).willReturn(Optional.empty());
        given(threadRepository.save(any(MessageThread.class))).willAnswer(invocation -> {
            MessageThread saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 500L);
            return saved;
        });
        given(itemRepository.save(any(MessageItem.class))).willAnswer(invocation -> invocation.getArgument(0));

        Long threadId = messageService.sendInquiry(100L, 2L, request);

        assertThat(threadId).isEqualTo(500L);
        verify(itemRepository).save(any(MessageItem.class));
    }

    @Test
    void 문의보내기_실패_본인모임() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        Team team = buildTeam(100L, leader);
        InquiryRequest request = new InquiryRequest("제목", "내용");

        given(teamRepository.findById(100L)).willReturn(Optional.of(team));

        assertThatThrownBy(() -> messageService.sendInquiry(100L, 1L, request))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 답장_성공() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        User applicant = buildUser(2L, "member@test.com", "지원자");
        Team team = buildTeam(100L, leader);
        MessageThread thread = buildThread(500L, team, applicant, leader);
        ReplyRequest request = new ReplyRequest("네 5명이에요!");

        given(threadRepository.findById(500L)).willReturn(Optional.of(thread));
        given(userRepository.findById(1L)).willReturn(Optional.of(leader));
        given(itemRepository.save(any(MessageItem.class))).willAnswer(invocation -> {
            MessageItem saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 900L);
            return saved;
        });

        Long messageId = messageService.reply(500L, 1L, request);

        assertThat(messageId).isEqualTo(900L);
    }

    @Test
    void 답장_실패_참여자아님() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        User applicant = buildUser(2L, "member@test.com", "지원자");
        User stranger = buildUser(3L, "stranger@test.com", "제3자");
        Team team = buildTeam(100L, leader);
        MessageThread thread = buildThread(500L, team, applicant, leader);
        ReplyRequest request = new ReplyRequest("몰래 끼어들기");

        given(threadRepository.findById(500L)).willReturn(Optional.of(thread));

        assertThatThrownBy(() -> messageService.reply(500L, stranger.getId(), request))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 쪽지함조회_안읽음과읽음분류() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        User applicant = buildUser(2L, "member@test.com", "지원자");
        Team team = buildTeam(100L, leader);
        MessageThread threadA = buildThread(500L, team, applicant, leader); // 안읽음
        MessageThread threadB = buildThread(501L, team, applicant, leader); // 읽음

        given(threadRepository.findAllByParticipant(1L)).willReturn(List.of(threadA, threadB));
        given(itemRepository.countByThreadIdAndSenderIdNotAndReadFalse(500L, 1L)).willReturn(1L);
        given(itemRepository.countByThreadIdAndSenderIdNotAndReadFalse(501L, 1L)).willReturn(0L);
        given(itemRepository.findTopByThreadIdOrderByCreatedAtDesc(500L)).willReturn(Optional.empty());
        given(itemRepository.findTopByThreadIdOrderByCreatedAtDesc(501L)).willReturn(Optional.empty());

        InboxResponse inbox = messageService.getInbox(1L);

        assertThat(inbox.unread()).hasSize(1);
        assertThat(inbox.read()).hasSize(1);
        assertThat(inbox.unread().get(0).threadId()).isEqualTo(500L);
    }

    @Test
    void 대화방상세조회_읽음처리됨() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        User applicant = buildUser(2L, "member@test.com", "지원자");
        Team team = buildTeam(100L, leader);
        MessageThread thread = buildThread(500L, team, applicant, leader);

        MessageItem unreadItem = MessageItem.builder().thread(thread).sender(applicant).content("안읽은 메시지").build();
        ReflectionTestUtils.setField(unreadItem, "id", 1L);

        given(threadRepository.findById(500L)).willReturn(Optional.of(thread));
        given(itemRepository.findByThreadIdAndSenderIdNotAndReadFalse(500L, 1L)).willReturn(List.of(unreadItem));
        given(itemRepository.findByThreadIdOrderByCreatedAtAsc(500L)).willReturn(List.of(unreadItem));

        ThreadDetailResponse detail = messageService.getThreadDetail(500L, 1L);

        assertThat(detail.messages()).hasSize(1);
        assertThat(unreadItem.isRead()).isTrue();
        assertThat(detail.otherNickname()).isEqualTo("지원자");
    }

    @Test
    void 대화방상세조회_실패_참여자아님() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        User applicant = buildUser(2L, "member@test.com", "지원자");
        User stranger = buildUser(3L, "stranger@test.com", "제3자");
        Team team = buildTeam(100L, leader);
        MessageThread thread = buildThread(500L, team, applicant, leader);

        given(threadRepository.findById(500L)).willReturn(Optional.of(thread));

        assertThatThrownBy(() -> messageService.getThreadDetail(500L, stranger.getId()))
                .isInstanceOf(CustomException.class);
    }
}
