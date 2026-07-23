package com.meetory.message.dto;

import jakarta.validation.constraints.NotBlank;

// 채팅형 팝업 하단 입력창에서 보내는 답장
public record ReplyRequest(

    @NotBlank(message = "메시지를 입력해주세요")
    String content

) {
}
