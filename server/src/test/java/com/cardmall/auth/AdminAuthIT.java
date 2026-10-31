package com.cardmall.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminAuthIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void login_with_seeded_admin_returns_token() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800000000\",\"password\":\"123456\"}"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void wrong_password_is_rejected_with_3001() throws Exception {
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800000000\",\"password\":\"wrong-pwd\"}"))
                .andExpect(jsonPath("$.code").value(3001));
    }

    @Test
    void staff_login_uses_same_account_but_other_login_type() throws Exception {
        mockMvc.perform(post("/api/staff/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800000000\",\"password\":\"123456\"}"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void same_wx_code_logs_into_same_member() throws Exception {
        Long first = loginMember("same-code-member");
        Long second = loginMember("same-code-member");
        assertThat(second).isEqualTo(first);
    }

    private Long loginMember(String code) throws Exception {
        String body = mockMvc.perform(post("/api/app/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\"}"))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.path("data").path("id").asLong();
    }
}
