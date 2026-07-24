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

// "문의하기" 로 시작되는 1:1 대화방.
// 같은 모임(team) + 같은 두 사람(starter, leader) 조합이면 대화방을 재사용해서
// 카카오톡/인스타 DM 처럼 답장이 한 스레드에 계속 쌓이도록 한다.
@Entity
@Table(
    name = "message_thread",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_thread_team_starter_leader",
        columnNames = {"team_id", "starter_id", "leader_id"}
    )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Column(name = "team_title_snapshot", length = 100)
    private String teamTitleSnapshot;

    // 문의를 처음 보낸 사람 (신청자 등 일반 유저)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "starter_id", nullable = false)
    private User starter;

    // 문의를 받는 모임 리더
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    // 최초 문의 제목 (마이페이지 목록에 노출)
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

    public void touch() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public void detachFromTeam(String teamTitle) {
        this.teamTitleSnapshot = teamTitle;
        this.team = null;
    }

    public String resolvedTeamTitle() {
        if (team != null) {
            return team.getTitle();
        }
        return teamTitleSnapshot != null ? teamTitleSnapshot : "";
    }

    public Long resolvedTeamId() {
        return team != null ? team.getId() : null;
    }

    public boolean isParticipant(Long userId) {
        return starter.getId().equals(userId) || leader.getId().equals(userId);
    }

    // 대화 상대방(나 말고 다른 한 명) 조회
    public User theOther(Long userId) {
        return starter.getId().equals(userId) ? leader : starter;
    }
}
