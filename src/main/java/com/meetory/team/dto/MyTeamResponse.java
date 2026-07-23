package com.meetory.team.dto;

import java.time.LocalDateTime;

import com.meetory.member.entity.Member;
import com.meetory.team.entity.Team;

// "모임 관리" 화면 - 내가 속한 모임 목록에 사용
public record MyTeamResponse(
    Long teamId,
    String title,
    String category,
    String status,
    int currentMembers,
    int maxMembers,
    boolean leader,
    LocalDateTime joinedAt
) {
    public static MyTeamResponse of(Member member, long currentMembers, Long userId) {
        Team team = member.getTeam();
        return new MyTeamResponse(
            team.getId(),
            team.getTitle(),
            team.getCategory().name(),
            team.getStatus().name(),
            (int) currentMembers,
            team.getMaxMembers(),
            team.isLeader(userId),
            member.getJoinedAt()
        );
    }
}