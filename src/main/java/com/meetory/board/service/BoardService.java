package com.meetory.board.service;

import com.meetory.board.dto.BoardCreateRequest;
import com.meetory.board.dto.BoardResponse;
import com.meetory.board.dto.BoardUpdateRequest;
import com.meetory.board.dto.BoardDetailResponse;
import com.meetory.board.entity.Board;
import com.meetory.board.repository.BoardRepository;
import com.meetory.common.exception.CustomException;
import com.meetory.common.exception.ErrorCode;
import com.meetory.user.entity.User;
import com.meetory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public BoardResponse createBoard(BoardCreateRequest request, Long userId) {
    	
        
        
    	User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Board board = Board.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .build();

        Board savedBoard = boardRepository.save(board);
        
        return BoardResponse.from(savedBoard);
    }
    
    @Transactional(readOnly = true)
    public List<BoardResponse> getBoardList() {
        return boardRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(BoardResponse::from)
                .toList();
    }
    @Transactional(readOnly = true)
    public BoardDetailResponse getBoardDetail(Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        return BoardDetailResponse.from(board);
    }
    
    @Transactional
    public BoardResponse updateBoard(Long boardId, BoardUpdateRequest request, Long userId) {
        // 1. 게시글 조회
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 2. 권한 체크 (글 작성자의 ID와 현재 로그인한 유저의 ID가 같은지)
        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION);
        }

        // 3. 게시글 수정 (JPA 변경 감지)
        board.update(request.title(), request.content());

        return BoardResponse.from(board);
    }
    
    @Transactional
    public void deleteBoard(Long boardId, Long userId) {
        // 1. 게시글 조회
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 2. 권한 체크 (글 작성자의 ID와 현재 로그인한 유저의 ID가 같은지)
        if (!board.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_ACTION);
        }

        // 3. 게시글 삭제
        boardRepository.delete(board);
    }
}