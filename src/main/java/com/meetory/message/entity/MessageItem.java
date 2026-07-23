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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String content;

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

	public void markAsRead() {
		this.read = true;
	}
}
