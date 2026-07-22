package com.meetory.board.dto;

import jakarta.validation.constraints.NotBlank;

public record BoardCreateRequest(
        @NotBlank(message = "제목을 입력하세요")
        String title,

        @NotBlank(message = "내용을 입력하세요")
        String content
) {
}