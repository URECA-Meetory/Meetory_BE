package com.meetory.team.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.meetory.team.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {

    // 목록(패널) 조회 시 leader(User) 를 함께 조인 -> N+1 방지
    @Query("select t from Team t join fetch t.leader order by t.createdAt desc")
    List<Team> findAllWithLeader();

    boolean existsByLeaderId(Long leaderId);

    List<Team> findByLeaderId(Long leaderId);
}
