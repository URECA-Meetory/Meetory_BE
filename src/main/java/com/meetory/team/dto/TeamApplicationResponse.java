package com.meetory.team.dto;

import java.time.LocalDateTime;

import com.meetory.member.entity.Member;

// 리더가 확인하는 "대기중" 신청 목록 (수락/거절 대상). 리더만 조회 가능하므로 email 까지 포함
public record TeamApplicationResponse(
    Long memberId,
    Long userId,
    String nickname,
    String email,
    String status,
    LocalDateTime appliedAt
) {
    public static TeamApplicationResponse of(Member member) {
        return new TeamApplicationResponse(
            member.getId(),
            member.getUser().getId(),
            member.getUser().getNickname(),
            member.getUser().getEmail(),
            member.getStatus().name(),
            member.getJoinedAt()
        );
    }
}
