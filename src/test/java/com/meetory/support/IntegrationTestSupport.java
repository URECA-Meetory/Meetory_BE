package com.meetory.support;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestSupport {

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    protected String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@test.com";
    }

    protected String uniqueNickname() {
        return "nick-" + UUID.randomUUID().toString().substring(0, 8);
    }

    protected void signup(String email, String password, String nickname) throws Exception {
        String body = """
                {"email":"%s","password":"%s","nickname":"%s"}
                """.formatted(email, password, nickname);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    protected String login(String email, String password) throws Exception {
        String body = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    protected long readLong(String json, String path) {
        Object value = JsonPath.read(json, path);
        return ((Number) value).longValue();
    }

    protected AuthSession signupAndLogin(String email, String password, String nickname) throws Exception {
        signup(email, password, nickname);
        String token = login(email, password);
        return new AuthSession(email, password, nickname, token);
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected String toJson(Map<String, ?> body) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : body.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(entry.getKey()).append('"').append(':');
            Object value = entry.getValue();
            if (value instanceof Number || value instanceof Boolean) {
                sb.append(value);
            } else {
                sb.append('"').append(String.valueOf(value).replace("\"", "\\\"")).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    protected record AuthSession(String email, String password, String nickname, String token) {
    }
}
