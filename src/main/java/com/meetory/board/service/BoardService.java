package com.meetory.board.service;

import com.meetory.board.dto.BoardCreateRequest;
import com.meetory.board.dto.BoardResponse;
import com.meetory.board.entity.Board;
import com.meetory.board.repository.BoardRepository;
import com.meetory.common.exception.CustomException;
import com.meetory.common.exception.ErrorCode;
import com.meetory.user.entity.User;
import com.meetory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    @Transactional
    public BoardResponse createBoard(BoardCreateRequest request) {
        // 추후 로그인 기능이 완성되면 SecurityContext에서 현재 인증된 유저 정보를 가져오도록 수정
        Long mockUserId = 1L; 
        
        User user = userRepository.findById(mockUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Board board = Board.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .build();

        Board savedBoard = boardRepository.save(board);
        
        return BoardResponse.from(savedBoard);
    }
}