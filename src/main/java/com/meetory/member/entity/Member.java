package com.meetory.member.entity;

import java.time.LocalDateTime;

import com.meetory.team.entity.Team;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Team - User 의 신청/가입 관계를 나타내는 조인 엔티티
// "신청하기" 버튼 -> Member(status=대기) 생성
// 기존 코드에서 team, user JoinColumn 이 둘 다 "user_id" 로 중복 지정되어 있던 버그를 "team_id" 로 수정하고
// 같은 유저가 같은 팀에 중복 신청하지 못하도록 (team_id, user_id) 유니크 제약을 추가함
@Entity
@Table(
    name = "member",
    uniqueConstraints = @UniqueConstraint(name = "uk_member_team_user", columnNames = {"team_id", "user_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false) // 기존 버그: "user_id" 로 잘못 지정되어 있었음
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
        this.status = MemberStatus.대기;  // 신청 시 무조건 대기 상태로 시작
    }

    @PrePersist
    public void prePersist() {
        this.joinedAt = LocalDateTime.now();
    }

    // ======== 비즈니스 메서드 ========

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
