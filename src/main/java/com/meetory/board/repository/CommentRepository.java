package com.meetory.board.repository;

import com.meetory.board.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardIdOrderByCreatedAtAsc(Long boardId);
}