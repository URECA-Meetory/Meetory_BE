package com.meetory.board.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meetory.board.dto.BoardCreateRequest;
import com.meetory.board.dto.BoardDetailResponse;
import com.meetory.board.dto.BoardResponse;
import com.meetory.board.dto.BoardUpdateRequest;
import com.meetory.board.service.BoardService;
import com.meetory.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<ApiResponse<BoardResponse>> createBoard(
            @Valid @RequestBody BoardCreateRequest request,
            @AuthenticationPrincipal Long userId) {
        BoardResponse response = boardService.createBoard(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("게시글이 성공적으로 작성되었습니다", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BoardResponse>>> getBoardList() {
        List<BoardResponse> response = boardService.getBoardList();
        return ResponseEntity.ok(ApiResponse.success("게시글 목록 조회 완료", response));
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardDetailResponse>> getBoardDetail(
            @PathVariable("boardId") Long boardId) {
        BoardDetailResponse response = boardService.getBoardDetail(boardId);
        return ResponseEntity.ok(ApiResponse.success("게시글 상세 조회가 완료되었습니다", response));
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<ApiResponse<BoardResponse>> updateBoard(
            @PathVariable("boardId") Long boardId,
            @Valid @RequestBody BoardUpdateRequest request,
            @AuthenticationPrincipal Long userId) {
        BoardResponse response = boardService.updateBoard(boardId, request, userId);
        return ResponseEntity.ok(ApiResponse.success("게시글이 성공적으로 수정되었습니다", response));
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<ApiResponse<Void>> deleteBoard(
            @PathVariable("boardId") Long boardId,
            @AuthenticationPrincipal Long userId) {
        boardService.deleteBoard(boardId, userId);
        return ResponseEntity.ok(ApiResponse.success("게시글이 성공적으로 삭제되었습니다", null));
    }
}
