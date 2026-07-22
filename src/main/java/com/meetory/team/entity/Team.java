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

// 잡코리아 채용공고 패널 형태의 "모임(팀)" 엔티티
// 목록 패널 : title, category, description 요약, 인원현황, 상태
// 상세 팝업 : description 전문, leader, createdAt 등
@Entity
@Table(name = "team")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TeamCategory category;

    // 모임 소개(상세 팝업 전문 노출용) - 길이 제한 없이 저장
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "max_members", nullable = false)
    private int maxMembers;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TeamStatus status;

    // 모임 개설자(방장) - 신청 시 leader 본인 여부 검증에 사용
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Team(String title, TeamCategory category, String description, int maxMembers, User leader) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.maxMembers = maxMembers;
        this.leader = leader;
        this.status = TeamStatus.모집중; // 개설 시 기본값
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // ======== 비즈니스 메서드 ========

    public void closeRecruiting() {
        this.status = TeamStatus.모집완료;
    }

    public boolean isRecruiting() {
        return this.status == TeamStatus.모집중;
    }

    public boolean isLeader(Long userId) {
        return this.leader.getId().equals(userId);
    }
}
