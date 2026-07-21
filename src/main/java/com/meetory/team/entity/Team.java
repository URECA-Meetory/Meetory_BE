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
import jakarta.persistence.Lob;
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
	
	// 모임 개설자 (다대일 : 여러 모임이 한 User에 속함)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@Column(nullable = false, length = 100)
	private String title;
	
	@Column(columnDefinition = "TEXT")
	private String description;
	
	@Column(nullable = false, length = 50)
	private String category;
	
	@Column(name = "max_member", nullable = false)
	private Integer maxMember;
	
	@Column(nullable = false, length = 100)
	@Enumerated(EnumType.STRING)
	private TeamStatus status;
	
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
	
	@Builder
	public Team(User user, String title, String description, String category, Integer maxMember) {
		this.user = user;
		this.title = title;
		this.description = description;
		this.category = category;
		this.maxMember = maxMember;
		this.status = TeamStatus.모집중;  // 개설시 무조건 모집중 시작
	}
	
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}
	
	// ======== 비즈니스 메서드 ==========
	
	// 개설자 본인인지 확인
	public boolean isOwner(User user) {
		return this.user.getId().equals(user.getId());
	}
	
	// 모집 완료 상태로 전환 (정원이 다 찼을 때 호출)
	public void closeRecruiting() {
		this.status = TeamStatus.모집완료;
	}
	
	// 모집 상태가 모집중인지 확인
	public boolean isRecruiting() {
		return this.status == TeamStatus.모집중;
	}
}
