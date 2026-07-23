package com.meetory.team.entity;

import java.time.LocalDateTime;

import com.meetory.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "team")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "leader_id", nullable = false)
	private User leader;

	@Column(nullable = false, length = 100)
	private String title;

	@Column(nullable = false, columnDefinition = "LONGTEXT")
	private String description;

	@Column(nullable = false, length = 20)
	private String category;

	@Column(name = "max_members", nullable = false)
	private Integer maxMembers;

	@Column(nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private TeamStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Builder
	public Team(User leader, String title, String description, String category, Integer maxMembers) {
		this.leader = leader;
		this.title = title;
		this.description = description;
		this.category = category;
		this.maxMembers = maxMembers;
		this.status = TeamStatus.모집중;
	}

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

	public boolean isOwner(User user) {
		return this.leader.getId().equals(user.getId());
	}

	public void closeRecruiting() {
		this.status = TeamStatus.모집완료;
	}

	public boolean isRecruiting() {
		return this.status == TeamStatus.모집중;
	}
}
