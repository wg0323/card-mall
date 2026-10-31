package com.cardmall.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminCrudIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.update("delete from card_product");
        jdbcTemplate.update("delete from staff where phone <> '13800000000'");
    }

    @Test
    void create_count_card_then_list_it() throws Exception {
        String token = adminToken();
        mockMvc.perform(post("/api/admin/product").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(countCardBody("洗车5次卡", "50.00")))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/admin/product/page").header("Authorization", token)
                        .param("pageNum", "1").param("pageSize", "10"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].name").value("洗车5次卡"))
                .andExpect(jsonPath("$.data.list[0].totalTimes").value(5));
    }

    @Test
    void count_card_without_unit_price_is_rejected() throws Exception {
        mockMvc.perform(post("/api/admin/product").header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(countCardBody("洗车5次卡", "0")))
                .andExpect(jsonPath("$.code").value(1000));
    }

    @Test
    void update_store_then_read_it_back() throws Exception {
        String token = adminToken();
        mockMvc.perform(put("/api/admin/store").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"测试门店\",\"phone\":\"0571-88888888\",\"address\":\"西湖区1号\",\"businessHours\":\"09:00-21:00\"}"))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/admin/store").header("Authorization", token))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.name").value("测试门店"))
                .andExpect(jsonPath("$.data.address").value("西湖区1号"));
    }

    @Test
    void duplicated_staff_phone_is_rejected() throws Exception {
        String token = adminToken();
        String body = "{\"name\":\"小李\",\"phone\":\"13911112222\",\"role\":3}";
        mockMvc.perform(post("/api/admin/staff").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(jsonPath("$.code").value(0));
        mockMvc.perform(post("/api/admin/staff").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(jsonPath("$.code").value(3002));
    }

    @Test
    void disabled_staff_cannot_login() throws Exception {
        String token = adminToken();
        String body = "{\"name\":\"小王\",\"phone\":\"13911113333\",\"role\":3}";
        String created = mockMvc.perform(post("/api/admin/staff").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
        long staffId = objectMapper.readTree(created).path("data").asLong();

        mockMvc.perform(put("/api/admin/staff/" + staffId + "/status").header("Authorization", token)
                        .param("status", "0"))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/staff/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13911113333\",\"password\":\"123456\"}"))
                .andExpect(jsonPath("$.code").value(3001));
    }

    @Test
    void clerk_role_cannot_login_to_admin_console() throws Exception {
        String token = adminToken();
        mockMvc.perform(post("/api/admin/staff").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"店员甲\",\"phone\":\"13911114444\",\"role\":3}"))
                .andExpect(jsonPath("$.code").value(0));

        // 店员账号不能换后台权限
        mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13911114444\",\"password\":\"123456\"}"))
                .andExpect(jsonPath("$.code").value(3001));

        // 但店员端可以登录
        mockMvc.perform(post("/api/staff/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13911114444\",\"password\":\"123456\"}"))
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void missing_token_is_rejected_with_3000() throws Exception {
        mockMvc.perform(get("/api/admin/store"))
                .andExpect(jsonPath("$.code").value(3000));
    }

    @Test
    void member_token_cannot_call_admin_api() throws Exception {
        mockMvc.perform(get("/api/admin/store").header("Authorization", memberToken()))
                .andExpect(jsonPath("$.code").value(3000));
    }

    private String adminToken() throws Exception {
        String body = mockMvc.perform(post("/api/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800000000\",\"password\":\"123456\"}"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("token").asText();
    }

    private String memberToken() throws Exception {
        String body = mockMvc.perform(post("/api/app/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"crud-it-member\"}"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("token").asText();
    }

    private String countCardBody(String name, String unitPrice) {
        return "{\"name\":\"" + name + "\",\"cardType\":1,\"bizTag\":\"服务卡\",\"price\":199.00,"
                + "\"unitPrice\":" + unitPrice + ",\"totalTimes\":5,\"validDays\":365,"
                + "\"applicableService\":\"标准洗车\",\"purchaseNotice\":\"有效期365天\","
                + "\"status\":1,\"sort\":10}";
    }
}
