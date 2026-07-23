package com.meetory.team.dto;

import java.time.LocalDateTime;

import com.meetory.team.entity.Team;

// 패널의 설명 부분을 클릭했을 때 뜨는 팝업에서 사용하는 상세 정보
public record TeamDetailResponse(
    Long teamId,
    String title,
    String category,
    String description,      // 소개 전문
    Long leaderId,           // 프론트에서 "내가 리더인지"(신청 관리 UI 노출 여부) 판별용
    String leaderNickname,
    int currentMembers,
    int maxMembers,
    String status,
    LocalDateTime createdAt
) {
    public static TeamDetailResponse of(Team team, long currentMembers) {
        return new TeamDetailResponse(
            team.getId(),
            team.getTitle(),
            team.getCategory().name(),
            team.getDescription(),
            team.getLeader().getId(),
            team.getLeader().getNickname(),
            (int) currentMembers,
            team.getMaxMembers(),
            team.getStatus().name(),
            team.getCreatedAt()
        );
    }
}