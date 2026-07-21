package com.meetory.team.service;

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
import com.meetory.member.entity.Member;
import com.meetory.member.entity.MemberStatus;
import com.meetory.member.repository.MemberRepository;
import com.meetory.team.dto.TeamApplyResponse;
import com.meetory.team.dto.TeamCreateRequest;
import com.meetory.team.dto.TeamDetailResponse;
import com.meetory.team.dto.TeamListResponse;
import com.meetory.team.entity.Team;
import com.meetory.team.entity.TeamCategory;
import com.meetory.team.entity.TeamStatus;
import com.meetory.team.repository.TeamRepository;
import com.meetory.user.entity.Role;
import com.meetory.user.entity.User;
import com.meetory.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TeamService teamService;

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

    private Team buildTeam(Long id, User leader, int maxMembers, TeamStatus status) {
        Team team = Team.builder()
                .title("알고리즘 스터디")
                .category(TeamCategory.스터디)
                .description("매주 3회 알고리즘 문제풀이를 함께 하는 스터디입니다.")
                .maxMembers(maxMembers)
                .leader(leader)
                .build();
        ReflectionTestUtils.setField(team, "id", id);
        if (status != null) {
            ReflectionTestUtils.setField(team, "status", status);
        }
        return team;
    }

    @Test
    void 모임개설_성공() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        TeamCreateRequest request = new TeamCreateRequest("알고리즘 스터디", TeamCategory.스터디, "설명", 5);

        given(userRepository.findById(1L)).willReturn(Optional.of(leader));
        given(teamRepository.save(any(Team.class))).willAnswer(invocation -> {
            Team saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 100L);
            return saved;
        });

        Long teamId = teamService.createTeam(request, 1L);

        assertThat(teamId).isEqualTo(100L);
        verify(memberRepository).save(any(Member.class)); // 리더가 승인된 멤버로 자동 등록되는지 검증
    }

    @Test
    void 모임개설_실패_존재하지않는사용자() {
        TeamCreateRequest request = new TeamCreateRequest("알고리즘 스터디", TeamCategory.스터디, "설명", 5);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.createTeam(request, 999L))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 모임목록조회_성공() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        Team team = buildTeam(100L, leader, 5, TeamStatus.모집중);

        given(teamRepository.findAllWithLeader()).willReturn(List.of(team));
        given(memberRepository.countByTeamAndStatus(team, MemberStatus.승인)).willReturn(1L);

        List<TeamListResponse> result = teamService.getTeamList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("알고리즘 스터디");
        assertThat(result.get(0).currentMembers()).isEqualTo(1);
        assertThat(result.get(0).maxMembers()).isEqualTo(5);
    }

    @Test
    void 모임상세조회_성공() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        Team team = buildTeam(100L, leader, 5, TeamStatus.모집중);

        given(teamRepository.findById(100L)).willReturn(Optional.of(team));
        given(memberRepository.countByTeamAndStatus(team, MemberStatus.승인)).willReturn(2L);

        TeamDetailResponse detail = teamService.getTeamDetail(100L);

        assertThat(detail.teamId()).isEqualTo(100L);
        assertThat(detail.currentMembers()).isEqualTo(2);
        assertThat(detail.leaderNickname()).isEqualTo("리더");
    }

    @Test
    void 모임상세조회_실패_존재하지않음() {
        given(teamRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeamDetail(999L))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 모임신청_성공() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        User applicant = buildUser(2L, "member@test.com", "지원자");
        Team team = buildTeam(100L, leader, 5, TeamStatus.모집중);

        given(teamRepository.findById(100L)).willReturn(Optional.of(team));
        given(userRepository.findById(2L)).willReturn(Optional.of(applicant));
        given(memberRepository.existsByTeamAndUser(team, applicant)).willReturn(false);
        given(memberRepository.countByTeamAndStatus(team, MemberStatus.승인)).willReturn(1L);
        given(memberRepository.save(any(Member.class))).willAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", 1000L);
            return member;
        });

        TeamApplyResponse response = teamService.applyToTeam(100L, 2L);

        assertThat(response.teamId()).isEqualTo(100L);
        assertThat(response.status()).isEqualTo(MemberStatus.대기.name());
    }

    @Test
    void 모임신청_실패_본인모임() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        Team team = buildTeam(100L, leader, 5, TeamStatus.모집중);

        given(teamRepository.findById(100L)).willReturn(Optional.of(team));
        given(userRepository.findById(1L)).willReturn(Optional.of(leader));

        assertThatThrownBy(() -> teamService.applyToTeam(100L, 1L))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 모임신청_실패_이미신청() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        User applicant = buildUser(2L, "member@test.com", "지원자");
        Team team = buildTeam(100L, leader, 5, TeamStatus.모집중);

        given(teamRepository.findById(100L)).willReturn(Optional.of(team));
        given(userRepository.findById(2L)).willReturn(Optional.of(applicant));
        given(memberRepository.existsByTeamAndUser(team, applicant)).willReturn(true);

        assertThatThrownBy(() -> teamService.applyToTeam(100L, 2L))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 모임신청_실패_모집마감() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        User applicant = buildUser(2L, "member@test.com", "지원자");
        Team team = buildTeam(100L, leader, 5, TeamStatus.모집완료);

        given(teamRepository.findById(100L)).willReturn(Optional.of(team));
        given(userRepository.findById(2L)).willReturn(Optional.of(applicant));

        assertThatThrownBy(() -> teamService.applyToTeam(100L, 2L))
                .isInstanceOf(CustomException.class);
    }

    @Test
    void 모임신청_실패_정원초과() {
        User leader = buildUser(1L, "leader@test.com", "리더");
        User applicant = buildUser(2L, "member@test.com", "지원자");
        Team team = buildTeam(100L, leader, 2, TeamStatus.모집중);

        given(teamRepository.findById(100L)).willReturn(Optional.of(team));
        given(userRepository.findById(2L)).willReturn(Optional.of(applicant));
        given(memberRepository.existsByTeamAndUser(team, applicant)).willReturn(false);
        given(memberRepository.countByTeamAndStatus(team, MemberStatus.승인)).willReturn(2L);

        assertThatThrownBy(() -> teamService.applyToTeam(100L, 2L))
                .isInstanceOf(CustomException.class);
    }
}
