package com.meetory.message.dto;

import java.util.List;

// 마이페이지 쪽지 패널 - 안읽은 쪽지 / 읽은 쪽지 분리 응답
public record InboxResponse(
    List<ThreadSummaryResponse> unread,
    List<ThreadSummaryResponse> read
) {
}
