package com.meetory.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.meetory.support.IntegrationTestSupport;

@DisplayName("팀 매칭 API 통합 테스트")
class TeamMatchingIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("비로그인 사용자도 모임 목록을 조회할 수 있다")
    void list_teams_without_auth() throws Exception {
        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("모임 개설·신청·승인 흐름이 동작한다")
    void team_apply_and_approve_flow() throws Exception {
        AuthSession leader = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());
        AuthSession applicant = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());

        Map<String, Object> createBody = Map.of(
                "title", "알고리즘 스터디",
                "category", "스터디",
                "description", "매주 알고리즘 문제를 함께 풉니다.",
                "maxMembers", 5
        );

        MvcResult createResult = mockMvc.perform(post("/api/teams")
                        .header("Authorization", bearer(leader.token()))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(createBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        long teamId = readLong(createResult.getResponse().getContentAsString(), "$.data");

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.teamId==" + teamId + ")]").exists());

        mockMvc.perform(get("/api/teams/" + teamId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("알고리즘 스터디"));

        MvcResult applyResult = mockMvc.perform(post("/api/teams/" + teamId + "/apply")
                        .header("Authorization", bearer(applicant.token())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("대기"))
                .andReturn();

        long memberId = readLong(applyResult.getResponse().getContentAsString(), "$.data.memberId");

        mockMvc.perform(get("/api/teams/" + teamId + "/applications")
                        .header("Authorization", bearer(leader.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].memberId").value(memberId));

        mockMvc.perform(post("/api/teams/" + teamId + "/applications/" + memberId + "/approve")
                        .header("Authorization", bearer(leader.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/teams/" + teamId + "/members")
                        .header("Authorization", bearer(leader.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(get("/api/teams/my")
                        .header("Authorization", bearer(applicant.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.teamId==" + teamId + ")]").exists());
    }

    @Test
    @DisplayName("리더가 아닌 사용자는 신청 목록을 볼 수 없다")
    void applications_are_leader_only() throws Exception {
        AuthSession leader = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());
        AuthSession applicant = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());

        Map<String, Object> createBody = Map.of(
                "title", "독서 모임",
                "category", "독서",
                "description", "한 달에 한 권씩 읽습니다.",
                "maxMembers", 4
        );

        MvcResult createResult = mockMvc.perform(post("/api/teams")
                        .header("Authorization", bearer(leader.token()))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(createBody)))
                .andExpect(status().isCreated())
                .andReturn();

        long teamId = readLong(createResult.getResponse().getContentAsString(), "$.data");

        mockMvc.perform(post("/api/teams/" + teamId + "/apply")
                        .header("Authorization", bearer(applicant.token())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/teams/" + teamId + "/applications")
                        .header("Authorization", bearer(applicant.token())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("로그인 없이 모임을 개설할 수 없다")
    void create_team_requires_auth() throws Exception {
        Map<String, Object> body = Map.of(
                "title", "운동 모임",
                "category", "운동",
                "description", "함께 뛰어요",
                "maxMembers", 6
        );

        mockMvc.perform(post("/api/teams")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isUnauthorized());
    }
}
