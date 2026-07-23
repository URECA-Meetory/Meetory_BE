package com.meetory.message.dto;

import java.util.List;

// 쪽지 클릭 시 뜨는 채팅형 팝업 전체 데이터
public record ThreadDetailResponse(
    Long threadId,
    Long teamId,
    String teamTitle,
    String title,
    Long otherUserId,
    String otherNickname,
    List<MessageItemResponse> messages
) {
}
