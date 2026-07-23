package com.meetory.team.dto;

import java.time.LocalDateTime;

import com.meetory.member.entity.Member;

// 현재 팀에 속해있는(승인된) 멤버 목록 조회용 - 누구나 열람 가능하므로 최소 정보만 노출
public record TeamMemberResponse(
    Long memberId,
    Long userId,
    String nickname,
    LocalDateTime joinedAt
) {
    public static TeamMemberResponse of(Member member) {
        return new TeamMemberResponse(
            member.getId(),
            member.getUser().getId(),
            member.getUser().getNickname(),
            member.getJoinedAt()
        );
    }
}
