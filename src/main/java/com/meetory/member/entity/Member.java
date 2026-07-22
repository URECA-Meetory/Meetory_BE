package com.meetory.member.entity;

import java.time.LocalDateTime;

import com.meetory.user.entity.User;
import com.meetory.team.entity.Team;

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
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	//게시판 부분 테스트용으로 임시로 바꿈
	//원래코드 @JoinColumn(name = "user_id", nullable = false)
	@JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
	private Team team;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;
	
	@Column(nullable = false, length = 20)
	@Enumerated(EnumType.STRING)
	private MemberStatus status;
	
	@Column(name = "joined_at", nullable = false, updatable = false)
	private LocalDateTime joinedAt;
	
	@Builder
	public Member(Team team, User user) {
		this.team = team;
		this.user = user;
		this.status = MemberStatus.대기;  // 대기 신청시 무조건 대기 상태 시작
	}
	
	@PrePersist
	public void prePresist() {
		this.joinedAt = LocalDateTime.now();
	}
	
	// ======== 비지니스 메서드 ==========
	
	public void approve() {
		this.status = MemberStatus.승인;
	}
	
	public void reject() {
		this.status = MemberStatus.거절;
	}
	
	public boolean isPending() {
		return this.status == MemberStatus.대기;
	}
	
}
