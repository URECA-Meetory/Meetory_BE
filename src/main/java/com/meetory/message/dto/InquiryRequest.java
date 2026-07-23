package com.meetory.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// "문의하기" 팝업에서 보내는 최초 쪽지
public record InquiryRequest(

    @NotBlank(message = "쪽지 제목은 필수입니다")
    @Size(max = 100, message = "쪽지 제목은 100자 이하여야 합니다")
    String title,

    @NotBlank(message = "쪽지 내용은 필수입니다")
    String content

) {
}
