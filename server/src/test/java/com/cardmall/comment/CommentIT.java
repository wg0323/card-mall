package com.cardmall.comment;

import com.cardmall.entity.CardProduct;
import com.cardmall.mapper.CardProductMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CardProductMapper cardProductMapper;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.update("delete from card_comment");
        jdbcTemplate.update("delete from verify_record");
        jdbcTemplate.update("delete from member_card");
        jdbcTemplate.update("delete from pay_transaction");
        jdbcTemplate.update("delete from card_order");
        jdbcTemplate.update("delete from card_product");
        jdbcTemplate.update("delete from member_info");
    }

    @Test
    void can_comment_only_after_paid_order() throws Exception {
        Long productId = insertProduct("洗车5次卡");
        String token = memberToken("comment-eligibility");

        mockMvc.perform(get("/api/app/comment/eligibility").header("Authorization", token)
                        .param("productId", productId.toString()))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(false));

        String orderNo = createUnpaidOrder(token, productId);
        mockMvc.perform(get("/api/app/comment/eligibility").header("Authorization", token)
                        .param("productId", productId.toString()))
                .andExpect(jsonPath("$.data").value(false));

        payOrder(token, orderNo);
        mockMvc.perform(get("/api/app/comment/eligibility").header("Authorization", token)
                        .param("productId", productId.toString()))
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    void create_comment_with_four_images_then_duplicate_is_rejected() throws Exception {
        Long productId = insertProduct("洗车5次卡");
        String token = memberToken("comment-create");
        setNickname(token, "张三");
        createPaidOrder(token, productId);
        String images = ",\"images\":[\"https://example.com/1.jpg\",\"https://example.com/2.jpg\","
                + "\"https://example.com/3.jpg\",\"https://example.com/4.jpg\"]";
        Long commentId = createComment(token, productId, "服务很好", images);

        mockMvc.perform(get("/api/app/comment/page").header("Authorization", token)
                        .param("productId", productId.toString()))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].id").value(commentId))
                .andExpect(jsonPath("$.data.list[0].memberNickname").value("张三"))
                .andExpect(jsonPath("$.data.list[0].content").value("服务很好"))
                .andExpect(jsonPath("$.data.list[0].images.length()").value(4))
                .andExpect(jsonPath("$.data.list[0].images[0]").value("https://example.com/1.jpg"));

        mockMvc.perform(get("/api/app/comment/eligibility").header("Authorization", token)
                        .param("productId", productId.toString()))
                .andExpect(jsonPath("$.data").value(false));

        mockMvc.perform(post("/api/app/comment").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody(productId, "服务很好", images)))
                .andExpect(jsonPath("$.code").value(1104));
    }

    @Test
    void comment_requires_paid_order() throws Exception {
        Long productId = insertProduct("洗车5次卡");
        String token = memberToken("comment-unpaid");

        mockMvc.perform(post("/api/app/comment").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody(productId, "还没买卡", "")))
                .andExpect(jsonPath("$.code").value(1104));
    }

    @Test
    void order_specific_comment_uses_the_selected_order() throws Exception {
        Long productId = insertProduct("洗车5次卡");
        String token = memberToken("comment-order-specific");
        createPaidOrder(token, productId);
        String targetOrder = createPaidOrder(token, productId);

        mockMvc.perform(get("/api/app/comment/eligibility").header("Authorization", token)
                        .param("productId", productId.toString())
                        .param("orderNo", targetOrder))
                .andExpect(jsonPath("$.data").value(true));

        createComment(token, productId, targetOrder, "指定订单评论", "");

        mockMvc.perform(get("/api/app/comment/eligibility").header("Authorization", token)
                        .param("productId", productId.toString())
                        .param("orderNo", targetOrder))
                .andExpect(jsonPath("$.data").value(false));
        // 同产品的另一笔已支付订单仍可评
        mockMvc.perform(get("/api/app/comment/eligibility").header("Authorization", token)
                        .param("productId", productId.toString()))
                .andExpect(jsonPath("$.data").value(true));

        String commentOrderNo = jdbcTemplate.queryForObject(
                "select order_no from card_comment limit 1", String.class);
        assertThat(commentOrderNo).isEqualTo(targetOrder);
    }

    @Test
    void hidden_comment_is_not_visible_in_product_page_but_visible_in_my_page() throws Exception {
        Long productId = insertProduct("洗车5次卡");
        String token = memberToken("comment-hide");
        createPaidOrder(token, productId);
        Long commentId = createComment(token, productId, "服务很好", "");

        mockMvc.perform(put("/api/admin/comment/" + commentId + "/status").header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":2}"))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/app/comment/page").header("Authorization", token)
                        .param("productId", productId.toString()))
                .andExpect(jsonPath("$.data.total").value(0));

        mockMvc.perform(get("/api/app/comment/mine/page").header("Authorization", token))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].status").value(2));
    }

    @Test
    void admin_can_reply_and_clear_reply() throws Exception {
        Long productId = insertProduct("洗车5次卡");
        String token = memberToken("comment-reply");
        createPaidOrder(token, productId);
        Long commentId = createComment(token, productId, "服务很好", "");

        mockMvc.perform(put("/api/admin/comment/" + commentId + "/reply").header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"replyContent\":\"感谢支持\"}"))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/app/comment/page").header("Authorization", token)
                        .param("productId", productId.toString()))
                .andExpect(jsonPath("$.data.list[0].replyContent").value("感谢支持"));

        mockMvc.perform(put("/api/admin/comment/" + commentId + "/reply").header("Authorization", adminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"replyContent\":\"\"}"))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/app/comment/page").header("Authorization", token)
                        .param("productId", productId.toString()))
                .andExpect(jsonPath("$.data.list[0].replyContent").value(nullValue()));
    }

    @Test
    void user_can_delete_own_comment_and_cannot_delete_others() throws Exception {
        Long productId = insertProduct("洗车5次卡");
        String ownerToken = memberToken("comment-owner");
        createPaidOrder(ownerToken, productId);
        Long commentId = createComment(ownerToken, productId, "服务很好", "");

        String otherToken = memberToken("comment-other");
        mockMvc.perform(delete("/api/app/comment/" + commentId).header("Authorization", otherToken))
                .andExpect(jsonPath("$.code").value(1105));

        mockMvc.perform(delete("/api/app/comment/" + commentId).header("Authorization", ownerToken))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(get("/api/app/comment/mine/page").header("Authorization", ownerToken))
                .andExpect(jsonPath("$.data.total").value(0));
        mockMvc.perform(get("/api/app/comment/page").header("Authorization", ownerToken)
                        .param("productId", productId.toString()))
                .andExpect(jsonPath("$.data.total").value(0));

        mockMvc.perform(get("/api/admin/comment/page").header("Authorization", adminToken()))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.list[0].status").value(3));

        mockMvc.perform(post("/api/app/comment").header("Authorization", ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody(productId, "再评一次", "")))
                .andExpect(jsonPath("$.code").value(1104));
    }

    @Test
    void comment_rejects_more_than_four_images_and_blank_content() throws Exception {
        Long productId = insertProduct("洗车5次卡");
        String token = memberToken("comment-invalid");
        String fiveImages = ",\"images\":[\"https://example.com/1.jpg\",\"https://example.com/2.jpg\","
                + "\"https://example.com/3.jpg\",\"https://example.com/4.jpg\",\"https://example.com/5.jpg\"]";

        mockMvc.perform(post("/api/app/comment").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody(productId, "图片太多", fiveImages)))
                .andExpect(jsonPath("$.code").value(1000));

        mockMvc.perform(post("/api/app/comment").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody(productId, "   ", "")))
                .andExpect(jsonPath("$.code").value(1000));
    }

    // ---------- helpers ----------

    private Long insertProduct(String name) {
        CardProduct product = new CardProduct();
        product.setName(name);
        product.setCardType(CardProduct.TYPE_COUNT);
        product.setBizTag("");
        product.setPrice(new BigDecimal("199.00"));
        product.setUnitPrice(new BigDecimal("50.00"));
        product.setTotalTimes(5);
        product.setValidDays(365);
        product.setStatus(1);
        product.setSort(0);
        cardProductMapper.insert(product);
        return product.getId();
    }

    private void setNickname(String token, String nickname) throws Exception {
        mockMvc.perform(put("/api/app/member/profile").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"" + nickname + "\"}"))
                .andExpect(jsonPath("$.code").value(0));
    }

    private String createUnpaidOrder(String token, Long productId) throws Exception {
        String response = mockMvc.perform(post("/api/app/order").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + "}"))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").asText();
    }

    private void payOrder(String token, String orderNo) throws Exception {
        mockMvc.perform(post("/api/app/order/" + orderNo + "/mock-pay").header("Authorization", token))
                .andExpect(jsonPath("$.code").value(0));
    }

    private String createPaidOrder(String token, Long productId) throws Exception {
        String orderNo = createUnpaidOrder(token, productId);
        payOrder(token, orderNo);
        return orderNo;
    }

    private Long createComment(String token, Long productId, String content, String imagesFragment) throws Exception {
        return createComment(token, productId, null, content, imagesFragment);
    }

    private Long createComment(String token, Long productId, String orderNo, String content,
                               String imagesFragment) throws Exception {
        String response = mockMvc.perform(post("/api/app/comment").header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentBody(productId, orderNo, content, imagesFragment)))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).path("data").asLong();
    }

    private String commentBody(Long productId, String content, String imagesFragment) {
        return commentBody(productId, null, content, imagesFragment);
    }

    private String commentBody(Long productId, String orderNo, String content, String imagesFragment) {
        String orderPart = orderNo == null ? "" : ",\"orderNo\":\"" + orderNo + "\"";
        return "{\"productId\":" + productId + orderPart + ",\"content\":\"" + content + "\""
                + imagesFragment + "}";
    }

    private String adminToken() throws Exception {
        return token("/api/admin/auth/login", "{\"phone\":\"13800000000\",\"password\":\"123456\"}");
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
