package com.meetory.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import com.meetory.support.IntegrationTestSupport;

@DisplayName("모임 삭제 API 통합 테스트")
class TeamDeleteIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("리더가 모임을 삭제하면 멤버에게 안내 쪽지가 발송된다")
    void leader_can_delete_team_and_notify_members() throws Exception {
        AuthSession leader = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());
        AuthSession member = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());

        Map<String, Object> createBody = Map.of(
                "title", "삭제 테스트 모임",
                "category", "취미",
                "description", "삭제 테스트용 모임입니다.",
                "maxMembers", 5
        );

        MvcResult createResult = mockMvc.perform(post("/api/teams")
                        .header("Authorization", bearer(leader.token()))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(createBody)))
                .andExpect(status().isCreated())
                .andReturn();

        long teamId = readLong(createResult.getResponse().getContentAsString(), "$.data");

        MvcResult applyResult = mockMvc.perform(post("/api/teams/" + teamId + "/apply")
                        .header("Authorization", bearer(member.token())))
                .andExpect(status().isCreated())
                .andReturn();

        long memberId = readLong(applyResult.getResponse().getContentAsString(), "$.data.memberId");

        mockMvc.perform(post("/api/teams/" + teamId + "/applications/" + memberId + "/approve")
                        .header("Authorization", bearer(leader.token())))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/teams/" + teamId)
                        .header("Authorization", bearer(leader.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/teams/" + teamId))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/messages/threads")
                        .header("Authorization", bearer(member.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unread[0].title").value("[모임 삭제] 삭제 테스트 모임"));
    }

    @Test
    @DisplayName("리더가 아닌 사용자는 모임을 삭제할 수 없다")
    void non_leader_cannot_delete_team() throws Exception {
        AuthSession leader = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());
        AuthSession member = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());

        Map<String, Object> createBody = Map.of(
                "title", "권한 테스트",
                "category", "운동",
                "description", "권한 테스트 모임",
                "maxMembers", 4
        );

        MvcResult createResult = mockMvc.perform(post("/api/teams")
                        .header("Authorization", bearer(leader.token()))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(createBody)))
                .andExpect(status().isCreated())
                .andReturn();

        long teamId = readLong(createResult.getResponse().getContentAsString(), "$.data");

        mockMvc.perform(delete("/api/teams/" + teamId)
                        .header("Authorization", bearer(member.token())))
                .andExpect(status().isForbidden());
    }
}
