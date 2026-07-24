package com.meetory.message.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.meetory.message.entity.MessageThread;

public interface MessageThreadRepository extends JpaRepository<MessageThread, Long> {

    // 마이페이지 쪽지함 - 내가 참여중인(발신자 또는 리더인) 모든 대화방, 최근 대화 순
    @Query("select t from MessageThread t "
            + "left join fetch t.team join fetch t.starter join fetch t.leader "
            + "where t.starter.id = :userId or t.leader.id = :userId "
            + "order by t.lastMessageAt desc")
    List<MessageThread> findAllByParticipant(@Param("userId") Long userId);

    // 같은 모임 + 같은 두 사람이면 기존 대화방을 재사용 (카톡처럼 한 스레드에 이어지도록)
    Optional<MessageThread> findByTeamIdAndStarterIdAndLeaderId(Long teamId, Long starterId, Long leaderId);

    List<MessageThread> findByStarterId(Long starterId);

    List<MessageThread> findByLeaderId(Long leaderId);

    List<MessageThread> findByTeamId(Long teamId);

    void deleteByStarterId(Long starterId);
}
