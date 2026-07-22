package com.meetory.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.meetory.member.entity.Member;
import com.meetory.member.entity.MemberStatus;
import com.meetory.team.entity.Team;
import com.meetory.user.entity.User;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 신청하기 클릭 시 중복 신청 여부 체크
    boolean existsByTeamAndUser(Team team, User user);

    Optional<Member> findByTeamAndUser(Team team, User user);

    // 현재 인원 (승인된 멤버 수) - 패널/상세팝업의 "N/최대인원" 표기에 사용
    long countByTeamAndStatus(Team team, MemberStatus status);

    // 팀의 특정 상태 멤버 목록을 User 정보와 함께 조회 (N+1 방지)
    // - MemberStatus.승인 으로 조회 -> "현재 팀 멤버 목록" 화면
    // - MemberStatus.대기 으로 조회 -> 리더의 "신청 목록(수락/거절 대상)" 화면
    @Query("select m from Member m join fetch m.user where m.team.id = :teamId and m.status = :status order by m.joinedAt asc")
    List<Member> findByTeamIdAndStatus(@Param("teamId") Long teamId, @Param("status") MemberStatus status);
}
