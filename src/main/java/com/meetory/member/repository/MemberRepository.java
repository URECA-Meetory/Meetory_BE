package com.meetory.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.meetory.member.entity.Member;
import com.meetory.member.entity.MemberStatus;
import com.meetory.team.entity.Team;
import com.meetory.user.entity.User;

public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByTeamAndUser(Team team, User user);

    Optional<Member> findByTeamAndUser(Team team, User user);

    long countByTeamAndStatus(Team team, MemberStatus status);

    @Query("select m from Member m join fetch m.user where m.team.id = :teamId and m.status = :status order by m.joinedAt asc")
    List<Member> findByTeamIdAndStatus(@Param("teamId") Long teamId, @Param("status") MemberStatus status);

    // 모임 관리 화면 - 내가 속한(승인된) 모임 목록
    @Query("select m from Member m join fetch m.team t join fetch t.leader where m.user.id = :userId and m.status = :status order by m.joinedAt desc")
    List<Member> findByUserIdAndStatusWithTeam(@Param("userId") Long userId, @Param("status") MemberStatus status);

    void deleteByUserId(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Member m where m.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);
}