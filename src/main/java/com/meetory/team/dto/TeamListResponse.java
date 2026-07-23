package com.meetory.team.dto;

import com.meetory.team.entity.Team;

// 잡코리아 채용공고 목록 패널처럼 "간단한 설명 + 신청하기" 에 필요한 정보만 담는다.
public record TeamListResponse(
    Long teamId,
    String title,
    String category,
    String summary,          // 패널에 보여줄 짧은 소개 (description 요약)
    String leaderNickname,
    int currentMembers,
    int maxMembers,
    String status
) {
    private static final int SUMMARY_LENGTH = 80;

    public static TeamListResponse of(Team team, long currentMembers) {
        return new TeamListResponse(
            team.getId(),
            team.getTitle(),
            team.getCategory().name(),
            summarize(team.getDescription()),
            team.getLeader().getNickname(),
            (int) currentMembers,
            team.getMaxMembers(),
            team.getStatus().name()
        );
    }

    private static String summarize(String description) {
        if (description == null) {
            return "";
        }
        return description.length() > SUMMARY_LENGTH
            ? description.substring(0, SUMMARY_LENGTH) + "..."
            : description;
    }
}
