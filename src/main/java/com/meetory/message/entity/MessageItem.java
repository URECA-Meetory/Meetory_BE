package com.meetory.message.entity;

import java.time.LocalDateTime;

import com.meetory.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 대화방(MessageThread) 안의 말풍선 한 개.
// 최초 문의든 답장이든 동일한 구조로 저장되며, 채팅 UI에서는 createdAt 순으로 쭉 나열한다.
@Entity
@Table(name = "message_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private MessageThread thread;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;

    // 상대방(수신자) 기준 읽음 여부. 보낸 사람 본인 메시지는 항상 의미상 "읽음" 취급(조회 시 별도 처리).
    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MessageItem(MessageThread thread, User sender, String content) {
        this.thread = thread;
        this.sender = sender;
        this.content = content;
        this.read = false;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public void markRead() {
        this.read = true;
    }
}
