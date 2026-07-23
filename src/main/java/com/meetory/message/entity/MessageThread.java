package com.meetory.message.entity;

import java.time.LocalDateTime;

import com.meetory.team.entity.Team;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "message_thread",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_thread_team_starter_leader",
				columnNames = { "team_id", "starter_id", "leader_id" }
		)
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageThread {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "team_id", nullable = false)
	private Team team;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "starter_id", nullable = false)
	private User starter;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "leader_id", nullable = false)
	private User leader;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "last_message_at", nullable = false)
	private LocalDateTime lastMessageAt;

	@Builder
	public MessageThread(Team team, User starter, User leader, String title) {
		this.team = team;
		this.starter = starter;
		this.leader = leader;
		this.title = title;
	}

	@PrePersist
	public void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.lastMessageAt = now;
	}

	public void touchLastMessageAt() {
		this.lastMessageAt = LocalDateTime.now();
	}
}
