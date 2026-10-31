package com.cardmall.verify;

import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.entity.CardProduct;
import com.cardmall.mapper.CardProductMapper;
import com.cardmall.entity.MemberCard;
import com.cardmall.mapper.MemberCardMapper;
import com.cardmall.entity.MemberInfo;
import com.cardmall.entity.VerifyRecord;
import com.cardmall.mapper.MemberInfoMapper;
import com.cardmall.mapper.VerifyRecordMapper;
import com.cardmall.service.CardOrderService;
import com.cardmall.service.VerifyAttemptService;
import com.cardmall.service.VerifyCodeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class VerifyIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MemberInfoMapper memberInfoMapper;
    @Autowired
    private CardProductMapper cardProductMapper;
    @Autowired
    private MemberCardMapper memberCardMapper;
    @Autowired
    private VerifyCodeService verifyCodeService;
    @Autowired
    private VerifyRecordMapper verifyRecordMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private VerifyAttemptService verifyAttemptService;

    @Value("${card-mall.store-id:1}")
    private Long mainStoreId;

    @Value("${card-mall.verify.max-fail-times-per-store:30}")
    private int storeMaxFailTimes;

    private Long memberId;
    private Long productId;

    @BeforeEach
    void setUp() {
        // 防刷计数与锁定都存 Redis，上一个用例的锁定不能带进下一个
        clearAttemptKeys();
        jdbcTemplate.update("delete from verify_record");
        jdbcTemplate.update("delete from member_card");
        jdbcTemplate.update("delete from card_order");
        jdbcTemplate.update("delete from pay_transaction");
        jdbcTemplate.update("delete from card_product");

        MemberInfo member = new MemberInfo();
        member.setOpenId("verify-it-" + System.nanoTime());
        member.setNickname("张三");
        member.setAvatar("");
        member.setPhone("13700001111");
        memberInfoMapper.insert(member);
        memberId = member.getId();

        CardProduct product = new CardProduct();
        product.setName("洗车5次卡");
        product.setCardType(CardProduct.TYPE_COUNT);
        product.setBizTag("服务卡");
        product.setPrice(new BigDecimal("199.00"));
        product.setUnitPrice(new BigDecimal("50.00"));
        product.setTotalTimes(5);
        product.setValidDays(365);
        product.setStatus(1);
        product.setSort(0);
        cardProductMapper.insert(product);
        productId = product.getId();
    }

    // ---------- T6：下单与 mock 支付 ----------

    @Test
    void mock_pay_twice_issues_only_one_card() throws Exception {
        String memberToken = memberToken();
        String orderNo = mockMvc.perform(post("/api/app/order").header("Authorization", memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + "}"))
                .andExpect(jsonPath("$.code").value(0))
                .andReturn().getResponse().getContentAsString()
                .replaceAll(".*\"data\":\"([^\"]+)\".*", "$1");

        payMock(memberToken, orderNo);
        payMock(memberToken, orderNo);

        Long cardCount = jdbcTemplate.queryForObject(
                "select count(*) from member_card", Long.class);
        assertThat(cardCount).isEqualTo(1L);
        Integer payStatus = jdbcTemplate.queryForObject(
                "select pay_status from card_order where order_no = ?", Integer.class, orderNo);
        assertThat(payStatus).isEqualTo(1);
    }

    // ---------- T7：卡包与核销码 ----------

    @Test
    void code_expires_after_ttl() {
        Long cardId = createCountCard(5);
        String token = verifyCodeService.issue(memberId, cardId).getToken();

        sleepSeconds(3); // 测试 profile 里 TTL = 2 秒

        assertThat(verifyCodeService.consume(token)).isNull();
    }

    @Test
    void code_is_short_numeric_and_unique() {
        Long cardId = createCountCard(5);
        String first = verifyCodeService.issue(memberId, cardId).getToken();
        String second = verifyCodeService.issue(memberId, cardId).getToken();

        // 8 位、首位非 0：店员要能手输，且不会把前导 0 丢掉
        assertThat(first).matches("^[1-9]\\d{7}$");
        assertThat(second).matches("^[1-9]\\d{7}$");
        // 同一张卡同时存在的两个码不能撞
        assertThat(second).isNotEqualTo(first);
    }

    @Test
    void five_invalid_codes_lock_the_staff() throws Exception {
        String staff = staffToken();
        Long cardId = createCountCard(5);

        // "00000000" 在首位非 0 的生成规则下永远不会被签发，所以必定是无效码
        for (int attempt = 1; attempt <= 5; attempt++) {
            mockMvc.perform(post("/api/staff/verify/preview")
                            .header("Authorization", staff)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"00000000\"}"))
                    .andExpect(jsonPath("$.code").value(ErrorCode.VERIFY_CODE_INVALID));
        }

        // 锁定后即使拿着真码也不放行，提示的是“过于频繁”而不是“码已失效”
        String validToken = verifyCodeService.issue(memberId, cardId).getToken();
        mockMvc.perform(post("/api/staff/verify/preview")
                        .header("Authorization", staff)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + validToken + "\"}"))
                .andExpect(jsonPath("$.code").value(ErrorCode.VERIFY_CODE_INVALID))
                .andExpect(jsonPath("$.message").value("核销码尝试过于频繁，请稍后再试"));

        clearAttemptKeys();
    }

    @Test
    void store_gate_locks_after_store_wide_threshold() {
        // 不走 HTTP：直接打到闸门上，模拟多个账号轮流试的情形（每个账号都不超自己的额度）
        for (int attempt = 0; attempt < storeMaxFailTimes; attempt++) {
            verifyAttemptService.recordFailure(mainStoreId, 9001L + attempt);
        }

        // 即使是一个从没失败过的店员，也被门店层拦住
        assertThatThrownBy(() -> verifyAttemptService.checkNotLocked(mainStoreId, 8888L))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("门店");
    }

    @Test
    void issuing_code_for_others_card_is_rejected() {
        Long cardId = createCountCard(5);
        MemberInfo other = new MemberInfo();
        other.setOpenId("other-" + System.nanoTime());
        other.setNickname("李四");
        other.setAvatar("");
        other.setPhone("");
        memberInfoMapper.insert(other);

        try {
            verifyCodeService.issue(other.getId(), cardId);
            assertThat(false).as("他人卡不应能开码").isTrue();
        } catch (com.cardmall.common.BizException e) {
            assertThat(e.getCode()).isEqualTo(1101);
        }
    }

    // ---------- T8：核销 ----------

    @Test
    void deduct_two_at_once_leaves_three() throws Exception {
        Long cardId = createCountCard(5);
        String token = verifyCodeService.issue(memberId, cardId).getToken();
        String staffToken = staffToken();

        mockMvc.perform(post("/api/staff/verify/confirm").header("Authorization", staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody(token, "req-deduct-2", 2)))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.verifyTimes").value(2))
                .andExpect(jsonPath("$.data.remainingTimes").value(3));

        assertThat(remainingOf(cardId)).isEqualTo(3);
        VerifyRecord record = verifyRecordMapper.selectByRequestId("req-deduct-2");
        assertThat(record.getRemainingBefore()).isEqualTo(5);
        assertThat(record.getRemainingAfter()).isEqualTo(3);
    }

    @Test
    void deduct_more_than_remaining_is_rejected() throws Exception {
        Long cardId = createCountCard(3);
        String token = verifyCodeService.issue(memberId, cardId).getToken();

        mockMvc.perform(post("/api/staff/verify/confirm").header("Authorization", staffToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody(token, "req-too-many", 6)))
                .andExpect(jsonPath("$.code").value(2003));

        assertThat(remainingOf(cardId)).isEqualTo(3);
        assertThat(verifyRecordMapper.selectByRequestId("req-too-many")).isNull();
    }

    @Test
    void expired_card_is_rejected_at_confirm() throws Exception {
        Long cardId = createCountCard(5);
        String token = verifyCodeService.issue(memberId, cardId).getToken();
        // 开码时卡还在有效期，确认前过期：确认时按当前时间再校一次，不能只靠开码那一次
        jdbcTemplate.update("update member_card set valid_end = ? where id = ?",
                LocalDateTime.now().minusMinutes(1), cardId);

        mockMvc.perform(post("/api/staff/verify/confirm").header("Authorization", staffToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody(token, "req-expired", 1)))
                .andExpect(jsonPath("$.code").value(ErrorCode.CARD_NOT_USABLE));

        assertThat(remainingOf(cardId)).isEqualTo(5);
        assertThat(verifyRecordMapper.selectByRequestId("req-expired")).isNull();
    }

    @Test
    void issuing_code_on_used_up_card_is_rejected() throws Exception {
        Long cardId = createCountCard(1);
        String token = verifyCodeService.issue(memberId, cardId).getToken();
        mockMvc.perform(post("/api/staff/verify/confirm").header("Authorization", staffToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody(token, "req-last-time", 1)))
                .andExpect(jsonPath("$.code").value(0));

        assertThat(remainingOf(cardId)).isZero();
        assertThat(memberCardMapper.selectById(cardId).getStatus()).isEqualTo(MemberCard.STATUS_USED_UP);
        // 余 0 之后再核销：开码这一步就拦下，顾客不会拿到一个必然失败的码
        assertThatThrownBy(() -> verifyCodeService.issue(memberId, cardId))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("已用完");
    }

    @Test
    void same_request_id_deducts_once() throws Exception {
        Long cardId = createCountCard(5);
        String token = verifyCodeService.issue(memberId, cardId).getToken();
        String staffToken = staffToken();

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/staff/verify/confirm").header("Authorization", staffToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(confirmBody(token, "req-same", 1)));
        }

        assertThat(remainingOf(cardId)).isEqualTo(4);
        Long count = jdbcTemplate.queryForObject(
                "select count(*) from verify_record where request_id = 'req-same'", Long.class);
        assertThat(count).isEqualTo(1L);
    }

    @Test
    void replay_with_new_request_id_but_used_code_is_rejected() throws Exception {
        Long cardId = createCountCard(5);
        String token = verifyCodeService.issue(memberId, cardId).getToken();
        String staffToken = staffToken();

        mockMvc.perform(post("/api/staff/verify/confirm").header("Authorization", staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody(token, "req-first", 1)))
                .andExpect(jsonPath("$.code").value(0));

        mockMvc.perform(post("/api/staff/verify/confirm").header("Authorization", staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody(token, "req-second", 1)))
                .andExpect(jsonPath("$.code").value(2001));

        assertThat(remainingOf(cardId)).isEqualTo(4);
    }

    @Test
    void twenty_threads_each_deduct_once_cannot_oversell() throws Exception {
        Long cardId = createCountCard(5);
        String staffToken = staffToken();

        List<String> codes = new ArrayList<String>();
        for (int i = 0; i < 20; i++) {
            codes.add(verifyCodeService.issue(memberId, cardId).getToken());
        }

        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        for (int i = 0; i < 20; i++) {
            final int seq = i;
            pool.submit(() -> {
                try {
                    start.await();
                    String body = confirmBody(codes.get(seq), "req-concurrent-" + seq, 1);
                    String resp = mockMvc.perform(post("/api/staff/verify/confirm")
                                    .header("Authorization", staffToken)
                                    .contentType(MediaType.APPLICATION_JSON).content(body))
                            .andReturn().getResponse().getContentAsString();
                    if (objectMapper.readTree(resp).path("code").asInt() == 0) {
                        ok.incrementAndGet();
                    } else {
                        fail.incrementAndGet();
                    }
                } catch (Exception e) {
                    fail.incrementAndGet();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        pool.awaitTermination(60, TimeUnit.SECONDS);

        assertThat(ok.get()).isEqualTo(5);
        assertThat(fail.get()).isEqualTo(15);
        MemberCard card = memberCardMapper.selectById(cardId);
        assertThat(card.getRemainingTimes()).isZero();
        assertThat(card.getStatus()).isEqualTo(MemberCard.STATUS_USED_UP);
    }

    @Test
    void period_card_deducts_nothing() throws Exception {
        Long cardId = createPeriodCard();
        String token = verifyCodeService.issue(memberId, cardId).getToken();

        mockMvc.perform(post("/api/staff/verify/confirm").header("Authorization", staffToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(confirmBody(token, "req-period", 1)))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.verifyTimes").value(0));

        MemberCard card = memberCardMapper.selectById(cardId);
        assertThat(card.getRemainingTimes()).isZero();
        assertThat(card.getStatus()).isEqualTo(MemberCard.STATUS_NORMAL);
    }

    @Test
    void twenty_threads_each_deduct_two_leaves_one() throws Exception {
        Long cardId = createCountCard(5);
        String staffToken = staffToken();

        List<String> codes = new ArrayList<String>();
        for (int i = 0; i < 20; i++) {
            codes.add(verifyCodeService.issue(memberId, cardId).getToken());
        }

        ExecutorService pool = Executors.newFixedThreadPool(20);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        for (int i = 0; i < 20; i++) {
            final int seq = i;
            pool.submit(() -> {
                try {
                    start.await();
                    String body = confirmBody(codes.get(seq), "req-two-" + seq, 2);
                    String resp = mockMvc.perform(post("/api/staff/verify/confirm")
                                    .header("Authorization", staffToken)
                                    .contentType(MediaType.APPLICATION_JSON).content(body))
                            .andReturn().getResponse().getContentAsString();
                    if (objectMapper.readTree(resp).path("code").asInt() == 0) {
                        ok.incrementAndGet();
                    } else {
                        fail.incrementAndGet();
                    }
                } catch (Exception e) {
                    fail.incrementAndGet();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        pool.awaitTermination(60, TimeUnit.SECONDS);

        assertThat(ok.get()).isEqualTo(2);
        assertThat(fail.get()).isEqualTo(18);
        assertThat(remainingOf(cardId)).isEqualTo(1);
    }

    // ---------- helpers ----------

    private Long createCountCard(int totalTimes) {
        MemberCard card = new MemberCard();
        card.setCardNo("C" + System.nanoTime());
        card.setMemberId(memberId);
        card.setProductId(productId);
        card.setOrderNo("O" + System.nanoTime());
        card.setCardName("洗车5次卡");
        card.setCardType(MemberCard.TYPE_COUNT);
        card.setTotalTimes(totalTimes);
        card.setRemainingTimes(totalTimes);
        card.setValidStart(LocalDateTime.now().minusDays(1));
        card.setValidEnd(LocalDateTime.now().plusDays(30));
        card.setStatus(MemberCard.STATUS_NORMAL);
        memberCardMapper.insert(card);
        return card.getId();
    }

    private Long createPeriodCard() {
        MemberCard card = new MemberCard();
        card.setCardNo("P" + System.nanoTime());
        card.setMemberId(memberId);
        card.setProductId(productId);
        card.setOrderNo("O" + System.nanoTime());
        card.setCardName("月卡");
        card.setCardType(MemberCard.TYPE_PERIOD);
        card.setTotalTimes(0);
        card.setRemainingTimes(0);
        card.setValidStart(LocalDateTime.now().minusDays(1));
        card.setValidEnd(LocalDateTime.now().plusDays(30));
        card.setStatus(MemberCard.STATUS_NORMAL);
        memberCardMapper.insert(card);
        return card.getId();
    }

    private int remainingOf(Long cardId) {
        return memberCardMapper.selectById(cardId).getRemainingTimes();
    }

    private String confirmBody(String token, String requestId, int times) {
        return "{\"token\":\"" + token + "\",\"requestId\":\"" + requestId + "\",\"times\":" + times + "}";
    }

    private void payMock(String memberToken, String orderNo) throws Exception {
        mockMvc.perform(post("/api/app/order/" + orderNo + "/mock-pay").header("Authorization", memberToken))
                .andExpect(jsonPath("$.code").value(0));
    }

    private String memberToken() throws Exception {
        String body = mockMvc.perform(post("/api/app/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"verify-it-member\"}"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("token").asText();
    }

    private String staffToken() throws Exception {
        String body = mockMvc.perform(post("/api/staff/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"13800000000\",\"password\":\"123456\"}"))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).path("data").path("token").asText();
    }

    private void sleepSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void clearAttemptKeys() {
        Set<String> keys = redisTemplate.keys("verify:fail:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        keys = redisTemplate.keys("verify:lock:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
