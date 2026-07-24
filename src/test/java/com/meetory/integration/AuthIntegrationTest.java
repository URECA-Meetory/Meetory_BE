package com.meetory.integration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.meetory.support.IntegrationTestSupport;

@DisplayName("인증 API 통합 테스트")
class AuthIntegrationTest extends IntegrationTestSupport {

    @Test
    @DisplayName("회원가입 후 로그인하고 로그아웃할 수 있다")
    void signup_login_logout() throws Exception {
        String email = uniqueEmail();
        String password = "Password1!";
        String nickname = uniqueNickname();

        signup(email, password, nickname);

        String token = login(email, password);

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.nickname").value(nickname));

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("이메일 중복 회원가입은 실패한다")
    void signup_duplicate_email() throws Exception {
        String email = uniqueEmail();
        String password = "Password1!";
        String nickname = uniqueNickname();

        signup(email, password, nickname);

        Map<String, String> body = Map.of(
                "email", email,
                "password", password,
                "nickname", "other-" + nickname
        );

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 실패한다")
    void login_invalid_password() throws Exception {
        String email = uniqueEmail();
        String password = "Password1!";
        signup(email, password, uniqueNickname());

        Map<String, String> body = Map.of(
                "email", email,
                "password", "WrongPass1!"
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(body)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("이메일 사용 가능 여부를 확인할 수 있다")
    void check_email_availability() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(get("/api/auth/check-email").param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(true));

        signup(email, "Password1!", uniqueNickname());

        mockMvc.perform(get("/api/auth/check-email").param("email", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(false));
    }
}
