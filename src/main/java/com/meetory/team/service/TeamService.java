package com.meetory.team.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meetory.common.exception.CustomException;
import com.meetory.common.exception.ErrorCode;
import com.meetory.member.entity.Member;
import com.meetory.member.entity.MemberStatus;
import com.meetory.member.repository.MemberRepository;
import com.meetory.team.dto.TeamApplicationResponse;
import com.meetory.team.dto.TeamApplyResponse;
import com.meetory.team.dto.TeamCreateRequest;
import com.meetory.team.dto.TeamDetailResponse;
import com.meetory.team.dto.TeamListResponse;
import com.meetory.team.dto.TeamMemberResponse;
import com.meetory.team.entity.Team;
import com.meetory.team.repository.TeamRepository;
import com.meetory.user.entity.User;
import com.meetory.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    // 모임 개설. 개설자는 자동으로 "승인" 상태의 멤버(리더)로 등록된다.
    @Transactional
    public Long createTeam(TeamCreateRequest request, Long leaderId) {
        User leader = userRepository.findById(leaderId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Team team = Team.builder()
                .title(request.title())
                .category(request.category())
                .description(request.description())
                .maxMembers(request.maxMembers())
                .leader(leader)
                .build();

        Team savedTeam = teamRepository.save(team);

        Member leaderMember = Member.builder()
                .team(savedTeam)
                .user(leader)
                .build();
        leaderMember.approve(); // 리더는 별도 승인 절차 없이 바로 승인 상태
        memberRepository.save(leaderMember);

        return savedTeam.getId();
    }

    // 잡코리아 채용공고 목록 스타일 - 패널에 뿌릴 간단한 정보 목록
    public List<TeamListResponse> getTeamList() {
        List<Team> teams = teamRepository.findAllWithLeader();
        return teams.stream()
                .map(team -> TeamListResponse.of(team, countApprovedMembers(team)))
                .collect(Collectors.toList());
    }

    // 패널의 설명 클릭 -> 팝업으로 띄울 상세 정보
    public TeamDetailResponse getTeamDetail(Long teamId) {
        Team team = findTeam(teamId);
        return TeamDetailResponse.of(team, countApprovedMembers(team));
    }

    // "신청하기" 버튼 -> 팀 가입 신청 (대기 상태로 Member 생성)
    @Transactional
    public TeamApplyResponse applyToTeam(Long teamId, Long userId) {
        Team team = findTeam(teamId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (team.isLeader(userId)) {
            throw new CustomException(ErrorCode.SELF_APPLY_NOT_ALLOWED);
        }

        if (!team.isRecruiting()) {
            throw new CustomException(ErrorCode.TEAM_NOT_RECRUITING);
        }

        if (memberRepository.existsByTeamAndUser(team, user)) {
            throw new CustomException(ErrorCode.ALREADY_APPLIED);
        }

        if (countApprovedMembers(team) >= team.getMaxMembers()) {
            throw new CustomException(ErrorCode.TEAM_FULL);
        }

        Member member = Member.builder()
                .team(team)
                .user(user)
                .build();
        Member savedMember = memberRepository.save(member);

        return TeamApplyResponse.of(savedMember);
    }

    // 현재 팀에 속해있는(승인된) 멤버 목록 - 누구나 열람 가능
    public List<TeamMemberResponse> getTeamMembers(Long teamId) {
        findTeam(teamId); // 존재하지 않는 팀이면 TEAM_NOT_FOUND
        return memberRepository.findByTeamIdAndStatus(teamId, MemberStatus.승인).stream()
                .map(TeamMemberResponse::of)
                .collect(Collectors.toList());
    }

    // 대기중인 신청 목록 - 리더 본인만 조회 가능
    public List<TeamApplicationResponse> getApplications(Long teamId, Long requesterId) {
        Team team = findTeam(teamId);
        requireLeader(team, requesterId);
        return memberRepository.findByTeamIdAndStatus(teamId, MemberStatus.대기).stream()
                .map(TeamApplicationResponse::of)
                .collect(Collectors.toList());
    }

    // 신청 수락 - 리더 본인만 처리 가능. 승인 시점에 다시 한번 정원을 체크한다.
    @Transactional
    public void approveApplication(Long teamId, Long memberId, Long requesterId) {
        Team team = findTeam(teamId);
        requireLeader(team, requesterId);
        Member member = findMemberInTeam(team, memberId);

        if (!member.isPending()) {
            throw new CustomException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }
        if (countApprovedMembers(team) >= team.getMaxMembers()) {
            throw new CustomException(ErrorCode.TEAM_FULL);
        }

        member.approve(); // 영속 상태 엔티티 - 트랜잭션 커밋 시점에 더티체킹으로 반영됨
    }

    // 신청 거절 - 리더 본인만 처리 가능
    @Transactional
    public void rejectApplication(Long teamId, Long memberId, Long requesterId) {
        Team team = findTeam(teamId);
        requireLeader(team, requesterId);
        Member member = findMemberInTeam(team, memberId);

        if (!member.isPending()) {
            throw new CustomException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }

        member.reject();
    }

    private void requireLeader(Team team, Long requesterId) {
        if (!team.isLeader(requesterId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_LEADER);
        }
    }

    private Member findMemberInTeam(Team team, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        if (!member.getTeam().getId().equals(team.getId())) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }
        return member;
    }

    private Team findTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
    }

    private long countApprovedMembers(Team team) {
        return memberRepository.countByTeamAndStatus(team, MemberStatus.승인);
    }
}
