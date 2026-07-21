package com.meetory.team.dto;

import com.meetory.member.entity.Member;

// "신청하기" 버튼 클릭 결과 응답
public record TeamApplyResponse(
    Long memberId,
    Long teamId,
    String status   // 대기 / 승인 / 거절
) {
    public static TeamApplyResponse of(Member member) {
        return new TeamApplyResponse(
            member.getId(),
            member.getTeam().getId(),
            member.getStatus().name()
        );
    }
}
