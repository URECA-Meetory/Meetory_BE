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

    // 팀 상세 화면에서 승인된 멤버 목록(User 정보까지 함께) 조회
    @Query("select m from Member m join fetch m.user where m.team.id = :teamId and m.status = :status")
    List<Member> findApprovedMembers(@Param("teamId") Long teamId, @Param("status") MemberStatus status);
}
