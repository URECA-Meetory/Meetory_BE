package com.meetory.board.dto;

import com.meetory.board.entity.Comment;
import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        String writerNickname,
        Long writerId,
        LocalDateTime createdAt
) {
    public static CommentResponse from(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getNickname(),
                comment.getUser().getId(),
                comment.getCreatedAt()
        );
    }
}