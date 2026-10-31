package com.cardmall.query;

import com.cardmall.entity.CardProduct;
import com.cardmall.mapper.CardProductMapper;
import com.cardmall.entity.MemberCard;
import com.cardmall.mapper.MemberCardMapper;
import com.cardmall.entity.MemberInfo;
import com.cardmall.mapper.MemberInfoMapper;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QueryIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CardProductMapper cardProductMapper;
    @Autowired
    private MemberCardMapper memberCardMapper;
    @Autowired
    private MemberInfoMapper memberInfoMapper;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.update("delete from verify_record");
        jdbcTemplate.update("delete from member_card");
        jdbcTemplate.update("delete from card_order");
        jdbcTemplate.update("delete from card_product");
        jdbcTemplate.update("delete from member_info");
    }

    @Test
    void staff_home_counts_only_today() throws Exception {
        Long cardId = createCardWithMember("13700000001");
        insertRecord(cardId, LocalDateTime.now());
        insertRecord(cardId, LocalDateTime.now());
        insertRecord(cardId, LocalDateTime.now().minusDays(2));

        mockMvc.perform(get("/api/staff/home").header("Authorization", staffToken()))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.todayVerifyCount").value(2));
    }

    @Test
    void verify_page_returns_record_with_card_and_member_name() throws Exception {
        Long cardId = createCardWithMember("13700000002");
        insertRecord(cardId, LocalDateTime.now());

        mockMvc.perform(get("/api/staff/verify/page").header("Authorization", staffToken())
                        .param("pageNum", "1").param("pageSize", "10"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].cardName").value("洗车5次卡"))
                .andExpect(jsonPath("$.data.list[0].memberNickname").value("张三"));
    }

    @Test
    void member_search_is_exact_phone_match() throws Exception {
        createCardWithMember("13700000003");

        mockMvc.perform(get("/api/staff/member/search").header("Authorization", staffToken())
                        .param("phone", "13700000003"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.member.phone").value("13700000003"));

        mockMvc.perform(get("/api/staff/member/search").header("Authorization", staffToken())
                        .param("phone", "1370000"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void app_home_returns_only_on_sale_products() throws Exception {
        insertProduct("在售卡", 1);
        insertProduct("下架卡", 0);

        mockMvc.perform(get("/api/app/home").header("Authorization", memberToken("home-it")))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.products.length()").value(1))
                .andExpect(jsonPath("$.data.products[0].name").value("在售卡"));
    }

    @Test
    void member_cannot_read_others_order() throws Exception {
        String ownerToken = memberToken("order-owner");
        String otherToken = memberToken("order-other");
        insertProduct("在售卡", 1);
        Long productId = jdbcTemplate.queryForObject(
                "select id from card_product where status = 1 limit 1", Long.class);

        String orderNo = mockMvc.perform(post("/api/app/order").header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + "}"))
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"data\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/app/order/" + orderNo).header("Authorization", otherToken))
                .andExpect(jsonPath("$.code").value(1102));
    }

    @Test
    void dashboard_always_has_seven_days() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", adminToken()))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.last7Days.length()").value(7));
    }

    // ---------- helpers ----------

    private Long createCardWithMember(String phone) {
        MemberInfo member = new MemberInfo();
        member.setOpenId("query-it-" + System.nanoTime());
        member.setNickname("张三");
        member.setAvatar("");
        member.setPhone(phone);
        memberInfoMapper.insert(member);

        MemberCard card = new MemberCard();
        card.setCardNo("Q" + System.nanoTime());
        card.setMemberId(member.getId());
        card.setProductId(1L);
        card.setOrderNo("O" + System.nanoTime());
        card.setCardName("洗车5次卡");
        card.setCardType(MemberCard.TYPE_COUNT);
        card.setTotalTimes(5);
        card.setRemainingTimes(5);
        card.setValidStart(LocalDateTime.now().minusDays(1));
        card.setValidEnd(LocalDateTime.now().plusDays(30));
        card.setStatus(MemberCard.STATUS_NORMAL);
        memberCardMapper.insert(card);
        return card.getId();
    }

    private void insertRecord(Long cardId, LocalDateTime verifyTime) {
        MemberCard card = memberCardMapper.selectById(cardId);
        jdbcTemplate.update(
                "insert into verify_record (record_no, request_id, member_card_id, member_id, staff_id, "
                        + "store_id, verify_times, remaining_before, remaining_after, status, remark, "
                        + "verify_time) values (?,?,?,?,?,?,?,?,?,?,?,?)",
                "R" + System.nanoTime(), "req-" + System.nanoTime(), cardId, card.getMemberId(),
                1L, 1L, 1, 5, 4, 1, "", verifyTime);
    }

    private Long insertProduct(String name, int status) {
        CardProduct product = new CardProduct();
        product.setName(name);
        product.setCardType(CardProduct.TYPE_COUNT);
        product.setBizTag("");
        product.setPrice(new BigDecimal("199.00"));
        product.setUnitPrice(new BigDecimal("50.00"));
        product.setTotalTimes(5);
        product.setValidDays(365);
        product.setStatus(status);
        product.setSort(0);
        cardProductMapper.insert(product);
        return product.getId();
    }

    private String adminToken() throws Exception {
        return token("/api/admin/auth/login", "{\"phone\":\"13800000000\",\"password\":\"123456\"}");
    }

    private String staffToken() throws Exception {
        return token("/api/staff/auth/login", "{\"phone\":\"13800000000\",\"password\":\"123456\"}");
    }

    private String memberToken(String code) throws Exception {
        return token("/api/app/auth/login", "{\"code\":\"" + code + "\"}");
    }

    private String token(String uri, String body) throws Exception {
        String resp = mockMvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resp).path("data").path("token").asText();
    }
}
