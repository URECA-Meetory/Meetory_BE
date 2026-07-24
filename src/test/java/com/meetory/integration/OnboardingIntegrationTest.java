package com.meetory.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.meetory.support.IntegrationTestSupport;

@DisplayName("온보딩 API 통합 테스트")
class OnboardingIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("회원가입 직후에는 온보딩이 완료되지 않은 상태다")
    void new_user_is_not_onboarded() throws Exception {
        AuthSession session = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", bearer(session.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(false));
    }

    @Test
    @DisplayName("온보딩 프로필을 저장하면 완료 상태가 된다")
    void complete_onboarding() throws Exception {
        AuthSession session = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());

        Map<String, Object> body = Map.of(
                "age", 28,
                "gender", "여성",
                "hobbies", "독서, 등산"
        );

        mockMvc.perform(put("/api/users/me/onboarding")
                        .header("Authorization", bearer(session.token()))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true))
                .andExpect(jsonPath("$.data.age").value(28))
                .andExpect(jsonPath("$.data.gender").value("여성"))
                .andExpect(jsonPath("$.data.hobbies").value("독서, 등산"));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", bearer(session.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true));
    }

    @Test
    @DisplayName("온보딩을 건너뛰면 완료 상태가 된다")
    void skip_onboarding() throws Exception {
        AuthSession session = signupAndLogin(uniqueEmail(), "Password1!", uniqueNickname());

        mockMvc.perform(post("/api/users/me/onboarding/skip")
                        .header("Authorization", bearer(session.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", bearer(session.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.onboardingCompleted").value(true));
    }

    @Test
    @DisplayName("인증 없이 온보딩 API를 호출하면 실패한다")
    void onboarding_requires_auth() throws Exception {
        mockMvc.perform(post("/api/users/me/onboarding/skip"))
                .andExpect(status().isUnauthorized());
    }
}
