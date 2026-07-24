package com.meetory.board.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.meetory.board.dto.BoardCreateRequest;
import com.meetory.board.dto.BoardDetailResponse;
import com.meetory.board.dto.BoardResponse;
import com.meetory.board.dto.BoardUpdateRequest;
import com.meetory.board.entity.Board;
import com.meetory.board.repository.BoardRepository;
import com.meetory.common.exception.CustomException;
import com.meetory.user.entity.Role;
import com.meetory.user.entity.User;
import com.meetory.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BoardService boardService;

    private User buildUser(Long id, String nickname) {
        User user = User.builder()
                .email(nickname + "@test.com")
                .password("encoded")
                .nickname(nickname)
                .role(Role.USER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Board buildBoard(Long id, User user, String title, String content) {
        Board board = Board.builder()
                .user(user)
                .title(title)
                .content(content)
                .build();
        ReflectionTestUtils.setField(board, "id", id);
        ReflectionTestUtils.setField(board, "createdAt", LocalDateTime.of(2026, 7, 23, 12, 0));
        ReflectionTestUtils.setField(board, "updatedAt", LocalDateTime.of(2026, 7, 23, 12, 0));
        return board;
    }

    @Test
    void 게시글_작성_성공() {
        User writer = buildUser(1L, "테스트유저");
        BoardCreateRequest request = new BoardCreateRequest("등산 모임 후기", "주말 등산 다녀왔어요.");

        given(userRepository.findById(1L)).willReturn(Optional.of(writer));
        given(boardRepository.save(any(Board.class))).willAnswer(invocation -> {
            Board saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 10L);
            ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.now());
            return saved;
        });

        BoardResponse response = boardService.createBoard(request, 1L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.title()).isEqualTo("등산 모임 후기");
        assertThat(response.writerNickname()).isEqualTo("테스트유저");
        verify(boardRepository).save(any(Board.class));
    }

    @Test
    void 게시글_작성_실패_사용자없음() {
        BoardCreateRequest request = new BoardCreateRequest("제목", "내용");
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.createBoard(request, 999L))
                .isInstanceOf(CustomException.class)
                .hasMessage("존재하지 않는 사용자입니다");
    }

    @Test
    void 게시글_목록_조회_성공() {
        User writer = buildUser(1L, "테스트유저");
        Board board = buildBoard(10L, writer, "독서 후기", "재미있게 읽었습니다.");

        given(boardRepository.findAllByOrderByCreatedAtDesc()).willReturn(List.of(board));

        List<BoardResponse> result = boardService.getBoardList();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("독서 후기");
        assertThat(result.get(0).writerNickname()).isEqualTo("테스트유저");
    }

    @Test
    void 게시글_상세_조회_성공() {
        User writer = buildUser(1L, "테스트유저");
        Board board = buildBoard(10L, writer, "요가 클래스 후기", "스트레칭이 시원했어요.");

        given(boardRepository.findById(10L)).willReturn(Optional.of(board));

        BoardDetailResponse response = boardService.getBoardDetail(10L);

        assertThat(response.title()).isEqualTo("요가 클래스 후기");
        assertThat(response.content()).isEqualTo("스트레칭이 시원했어요.");
        assertThat(response.writerNickname()).isEqualTo("테스트유저");
        assertThat(response.comments()).isEmpty();
    }

    @Test
    void 게시글_상세_조회_실패_없음() {
        given(boardRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.getBoardDetail(999L))
                .isInstanceOf(CustomException.class)
                .hasMessage("게시글을 찾을 수 없습니다");
    }

    @Test
    void 게시글_수정_성공() {
        User writer = buildUser(1L, "테스트유저");
        Board board = buildBoard(10L, writer, "旧 제목", "旧 내용");
        BoardUpdateRequest request = new BoardUpdateRequest("새 제목", "새 내용");

        given(boardRepository.findById(10L)).willReturn(Optional.of(board));

        BoardResponse response = boardService.updateBoard(10L, request, 1L);

        assertThat(response.title()).isEqualTo("새 제목");
        assertThat(response.content()).isEqualTo("새 내용");
    }

    @Test
    void 게시글_수정_실패_권한없음() {
        User writer = buildUser(1L, "작성자");
        Board board = buildBoard(10L, writer, "제목", "내용");
        BoardUpdateRequest request = new BoardUpdateRequest("변경", "변경");

        given(boardRepository.findById(10L)).willReturn(Optional.of(board));

        assertThatThrownBy(() -> boardService.updateBoard(10L, request, 2L))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 작업에 대한 권한이 없습니다");
    }

    @Test
    void 게시글_삭제_성공() {
        User writer = buildUser(1L, "테스트유저");
        Board board = buildBoard(10L, writer, "삭제할 글", "내용");

        given(boardRepository.findById(10L)).willReturn(Optional.of(board));

        boardService.deleteBoard(10L, 1L);

        verify(boardRepository).delete(board);
    }

    @Test
    void 게시글_삭제_실패_권한없음() {
        User writer = buildUser(1L, "작성자");
        Board board = buildBoard(10L, writer, "제목", "내용");

        given(boardRepository.findById(10L)).willReturn(Optional.of(board));

        assertThatThrownBy(() -> boardService.deleteBoard(10L, 2L))
                .isInstanceOf(CustomException.class)
                .hasMessage("해당 작업에 대한 권한이 없습니다");
    }
}
