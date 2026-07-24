package com.meetory.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.meetory.support.IntegrationTestSupport;

@DisplayName("게시판 API 통합 테스트")
class BoardIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("비로그인 사용자도 게시글 목록을 조회할 수 있다")
    void list_boards_without_auth() throws Exception {
        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("게시글 작성·조회·수정·삭제 흐름이 동작한다")
    void board_crud_flow() throws Exception {
        AuthSession writer = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());

        Map<String, String> createBody = Map.of(
                "title", "주말 등산 후기",
                "content", "날씨가 좋아서 등산하기 좋았습니다."
        );

        MvcResult createResult = mockMvc.perform(post("/api/boards")
                        .header("Authorization", bearer(writer.token()))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(createBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("주말 등산 후기"))
                .andExpect(jsonPath("$.data.writerNickname").value(writer.nickname()))
                .andReturn();

        long boardId = readLong(createResult.getResponse().getContentAsString(), "$.data.id");

        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(boardId));

        mockMvc.perform(get("/api/boards/" + boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("주말 등산 후기"))
                .andExpect(jsonPath("$.data.content").value("날씨가 좋아서 등산하기 좋았습니다."));

        Map<String, String> updateBody = Map.of(
                "title", "주말 등산 후기 (수정)",
                "content", "다음에도 같이 가요."
        );

        mockMvc.perform(put("/api/boards/" + boardId)
                        .header("Authorization", bearer(writer.token()))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(updateBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("주말 등산 후기 (수정)"));

        AuthSession other = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());

        mockMvc.perform(put("/api/boards/" + boardId)
                        .header("Authorization", bearer(other.token()))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(updateBody)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/boards/" + boardId)
                        .header("Authorization", bearer(writer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/boards/" + boardId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("로그인 없이 게시글을 작성할 수 없다")
    void create_board_requires_auth() throws Exception {
        Map<String, String> body = Map.of("title", "제목", "content", "내용");

        mockMvc.perform(post("/api/boards")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isUnauthorized());
    }
}
