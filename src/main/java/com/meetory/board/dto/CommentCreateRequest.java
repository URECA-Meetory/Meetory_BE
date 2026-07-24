package com.meetory.board.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
        @NotBlank(message = "댓글을 입력하세요")
        String content
) {
}