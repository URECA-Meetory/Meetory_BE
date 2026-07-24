package com.meetory.message.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.meetory.message.entity.MessageItem;

public interface MessageItemRepository extends JpaRepository<MessageItem, Long> {

    // 채팅 화면 - 대화방의 모든 말풍선을 시간순으로
    @Query("select m from MessageItem m join fetch m.sender where m.thread.id = :threadId order by m.createdAt asc")
    List<MessageItem> findByThreadIdOrderByCreatedAtAsc(@Param("threadId") Long threadId);

    // 대화방 목록에서 마지막 말풍선 미리보기용
    Optional<MessageItem> findTopByThreadIdOrderByCreatedAtDesc(Long threadId);

    // 특정 대화방에서 "내가 아직 안 읽은(상대방이 보낸)" 메시지 개수 - 안읽음/읽음 분류에 사용
    long countByThreadIdAndSenderIdNotAndReadFalse(Long threadId, Long myUserId);

    // 쪽지함 진입 시 안읽은 메시지 일괄 읽음 처리용
    List<MessageItem> findByThreadIdAndSenderIdNotAndReadFalse(Long threadId, Long myUserId);

    @Modifying(clearAutomatically = true)
    @Query("delete from MessageItem m where m.thread.id = :threadId")
    void deleteByThreadId(@Param("threadId") Long threadId);
}
