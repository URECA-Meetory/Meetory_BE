package com.meetory.board.dto;

import com.meetory.board.entity.Board;
import java.time.LocalDateTime;
import java.util.List;

public record BoardDetailResponse(
        Long id,
        String title,
        String content,
        String writerNickname,
        LocalDateTime createdAt,
        List<CommentResponse> comments 
) {
    public static BoardDetailResponse of(Board board, List<CommentResponse> comments) {
        return new BoardDetailResponse(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getUser().getNickname(),
                board.getCreatedAt(),
                comments
        );
    }
}