package com.meetory.message.dto;

import java.time.LocalDateTime;

// 마이페이지 "받은 쪽지" 목록 한 줄 (안읽음/읽음 분류에 사용)
public record ThreadSummaryResponse(
    Long threadId,
    Long teamId,
    String teamTitle,
    String title,
    String otherNickname,
    String lastMessagePreview,
    LocalDateTime lastMessageAt,
    boolean unread
) {
}
