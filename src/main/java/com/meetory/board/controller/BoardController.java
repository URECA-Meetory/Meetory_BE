package com.meetory.board.controller;

import com.meetory.board.dto.BoardCreateRequest;
import com.meetory.board.dto.BoardResponse;
import com.meetory.board.service.BoardService;
import com.meetory.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping
    public ResponseEntity<ApiResponse<BoardResponse>> createBoard(@Valid @RequestBody BoardCreateRequest request) {
        BoardResponse response = boardService.createBoard(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("게시글이 성공적으로 작성되었습니다", response));
    }
}