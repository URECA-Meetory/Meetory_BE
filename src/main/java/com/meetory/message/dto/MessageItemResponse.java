package com.meetory.message.dto;

import java.time.LocalDateTime;

// 채팅형 팝업의 말풍선 하나. mine 값으로 프론트에서 좌/우 정렬(내 말풍선 vs 상대 말풍선)을 결정한다.
public record MessageItemResponse(
    Long messageId,
    Long senderId,
    String senderNickname,
    String content,
    boolean mine,
    LocalDateTime createdAt
) {
}
