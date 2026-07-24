package com.meetory.board.repository;

import com.meetory.board.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    
    // 생성 시간 기준 최신순으로 모든 게시글 조회
    List<Board> findAllByOrderByCreatedAtDesc();

    void deleteByUserId(Long userId);
}