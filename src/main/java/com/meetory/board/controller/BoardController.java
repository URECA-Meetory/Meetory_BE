package com.meetory.board.controller;

import com.meetory.board.dto.BoardCreateRequest;
import com.meetory.board.dto.BoardDetailResponse;
import com.meetory.board.dto.BoardResponse;
import com.meetory.board.service.BoardService;
import com.meetory.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import com.meetory.board.dto.BoardUpdateRequest;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<ApiResponse<BoardResponse>> createBoard(
            @Valid @RequestBody BoardCreateRequest request,
            @AuthenticationPrincipal Long userId) { //토큰에서 인증된 userId를 받dma
            
        // 서비스로 진짜 userId를 넘겨줍니다.
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
    public ResponseEntity<ApiResponse<BoardDetailResponse>> getBoardDetail(@PathVariable("boardId") Long boardId) {
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