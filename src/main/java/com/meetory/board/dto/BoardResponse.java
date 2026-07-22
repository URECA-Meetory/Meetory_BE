package com.meetory.board.dto;

import com.meetory.board.entity.Board;
import java.time.LocalDateTime;

public record BoardResponse(
        Long id,
        String title,
        String content,
        String writerNickname,
        LocalDateTime createdAt
) {
    public static BoardResponse from(Board board) {
        return new BoardResponse(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getUser().getNickname(),
                board.getCreatedAt()
        );
    }
}