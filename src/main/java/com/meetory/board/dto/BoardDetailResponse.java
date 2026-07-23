package com.meetory.board.dto;

import com.meetory.board.entity.Board;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public record BoardDetailResponse(
        Long id,
        String title,
        String content,
        String writerNickname,
        LocalDateTime createdAt,
        List<Object> comments //나중에 댓글 기능 구현 시 CommentResponse DTO 리스트로 변경
) {
    public static BoardDetailResponse from(Board board) {
        return new BoardDetailResponse(
                board.getId(),
                board.getTitle(),
                board.getContent(),
                board.getUser().getNickname(),
                board.getCreatedAt(),
                new ArrayList<>() // 현재는 빈 리스트를 반환, 추후 게시글에 달린 댓글들을 매핑하여 반환
        );
    }
}