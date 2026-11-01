package com.cardmall;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class SchemaIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flyway_creates_core_tables() {
        List<String> tables = jdbcTemplate.queryForList(
                "select table_name from information_schema.tables where table_schema = database()",
                String.class);
        assertThat(tables).contains(
                "store", "staff", "member_info", "card_product",
                "card_order", "pay_transaction", "member_card", "verify_record", "card_comment",
                "member_message");
    }

    @Test
    void verify_record_has_idempotency_unique_index() {
        Long cnt = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.statistics "
                        + "where table_schema = database() and table_name = 'verify_record' "
                        + "and index_name = 'uk_request_id'", Long.class);
        assertThat(cnt).isEqualTo(1L);
    }

    @Test
    void main_store_is_seeded_with_id_one() {
        Long id = jdbcTemplate.queryForObject("select id from store where id = 1", Long.class);
        assertThat(id).isEqualTo(1L);
    }

    @Test
    void card_comment_has_order_unique_index() {
        Long cnt = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.statistics "
                        + "where table_schema = database() and table_name = 'card_comment' "
                        + "and index_name = 'uk_order_no'", Long.class);
        assertThat(cnt).isEqualTo(1L);
    }

    @Test
    void staff_has_avatar_column() {
        Long cnt = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.columns "
                        + "where table_schema = database() and table_name = 'staff' and column_name = 'avatar'",
                Long.class);
        assertThat(cnt).isEqualTo(1L);
    }
}
