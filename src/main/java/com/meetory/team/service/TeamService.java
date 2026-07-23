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
import com.meetory.team.dto.MyTeamResponse;
import com.meetory.team.dto.TeamApplicationResponse;
import com.meetory.team.dto.TeamApplyResponse;
import com.meetory.team.dto.TeamCreateRequest;
import com.meetory.team.dto.TeamDetailResponse;
import com.meetory.team.dto.TeamListResponse;
import com.meetory.team.dto.TeamMemberResponse;
import com.meetory.team.entity.Team;
import com.meetory.team.entity.TeamStatus;
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

    @Transactional
    public Long createTeam(TeamCreateRequest request, Long leaderId) {
        User leader = findUser(leaderId);

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
        leaderMember.approve();
        memberRepository.save(leaderMember);

        return savedTeam.getId();
    }

    public List<TeamListResponse> getTeamList() {
        List<Team> teams = teamRepository.findAllWithLeader();
        return teams.stream()
                .map(team -> TeamListResponse.of(team, countApprovedMembers(team)))
                .collect(Collectors.toList());
    }

    public TeamDetailResponse getTeamDetail(Long teamId) {
        Team team = findTeam(teamId);
        return TeamDetailResponse.of(team, countApprovedMembers(team));
    }

    @Transactional
    public TeamApplyResponse applyToTeam(Long teamId, Long userId) {
        Team team = findTeam(teamId);
        User user = findUser(userId);

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

    // 현재 팀에 속해있는(승인된) 멤버 목록 - 리더 또는 그 팀의 승인된 멤버만 열람 가능
    public List<TeamMemberResponse> getTeamMembers(Long teamId, Long requesterId) {
        Team team = findTeam(teamId);
        requireLeaderOrMember(team, requesterId);
        return memberRepository.findByTeamIdAndStatus(teamId, MemberStatus.승인).stream()
                .map(TeamMemberResponse::of)
                .collect(Collectors.toList());
    }

    public List<TeamApplicationResponse> getApplications(Long teamId, Long requesterId) {
        Team team = findTeam(teamId);
        requireLeader(team, requesterId);
        return memberRepository.findByTeamIdAndStatus(teamId, MemberStatus.대기).stream()
                .map(TeamApplicationResponse::of)
                .collect(Collectors.toList());
    }

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

        member.approve();

        // 승인으로 정원이 다 차면 자동으로 모집 마감 처리 (뱃지 = 모집완료)
        if (countApprovedMembers(team) >= team.getMaxMembers()) {
            team.closeRecruiting();
        }
    }

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

    // 모임 관리 화면 - 내가 속한(승인된) 모임 목록
    public List<MyTeamResponse> getMyTeams(Long userId) {
        List<Member> members = memberRepository.findByUserIdAndStatusWithTeam(userId, MemberStatus.승인);
        return members.stream()
                .map(m -> MyTeamResponse.of(m, countApprovedMembers(m.getTeam()), userId))
                .collect(Collectors.toList());
    }

    // 모임 탈퇴 - 리더는 탈퇴 불가, 승인된 멤버만 가능
    @Transactional
    public void leaveTeam(Long teamId, Long userId) {
        Team team = findTeam(teamId);
        if (team.isLeader(userId)) {
            throw new CustomException(ErrorCode.LEADER_CANNOT_LEAVE);
        }

        User user = findUser(userId);
        Member member = memberRepository.findByTeamAndUser(team, user)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() != MemberStatus.승인) {
            throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        }

        memberRepository.delete(member);

        // 탈퇴로 정원에 여유가 생기면 다시 모집중으로 전환
        if (team.getStatus() == TeamStatus.모집완료 && countApprovedMembers(team) < team.getMaxMembers()) {
            team.reopenRecruiting();
        }
    }

    private void requireLeader(Team team, Long requesterId) {
        if (!team.isLeader(requesterId)) {
            throw new CustomException(ErrorCode.NOT_TEAM_LEADER);
        }
    }

    private void requireLeaderOrMember(Team team, Long requesterId) {
        if (team.isLeader(requesterId)) {
            return;
        }
        User user = findUser(requesterId);
        boolean isApprovedMember = memberRepository.findByTeamAndUser(team, user)
                .map(m -> m.getStatus() == MemberStatus.승인)
                .orElse(false);
        if (!isApprovedMember) {
            throw new CustomException(ErrorCode.NOT_TEAM_MEMBER);
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

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private long countApprovedMembers(Team team) {
        return memberRepository.countByTeamAndStatus(team, MemberStatus.승인);
    }
}