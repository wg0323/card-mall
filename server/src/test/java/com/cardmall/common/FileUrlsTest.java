package com.cardmall.common;

import com.cardmall.config.FileProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * key 与访问地址互转的纯函数测试，不依赖 MinIO。
 */
class FileUrlsTest {

    @BeforeEach
    void setUp() {
        FileProperties properties = new FileProperties();
        properties.setEndpoint("http://127.0.0.1:9000");
        properties.setBucket("card-mall");
        new FileUrls(properties);
    }

    @Test
    void resolve_turns_key_into_url() {
        assertThat(FileUrls.resolve("card/2026/10/a.jpg"))
                .isEqualTo("http://127.0.0.1:9000/card-mall/card/2026/10/a.jpg");
    }

    @Test
    void resolve_keeps_third_party_url() {
        assertThat(FileUrls.resolve("https://cdn.example.com/a.jpg"))
                .isEqualTo("https://cdn.example.com/a.jpg");
    }

    @Test
    void resolve_returns_empty_for_blank() {
        assertThat(FileUrls.resolve(null)).isEmpty();
        assertThat(FileUrls.resolve("  ")).isEmpty();
    }

    @Test
    void normalize_turns_own_url_back_into_key() {
        assertThat(FileUrls.normalize("http://127.0.0.1:9000/card-mall/card/2026/10/a.jpg"))
                .isEqualTo("card/2026/10/a.jpg");
    }

    @Test
    void normalize_keeps_key_and_third_party_url() {
        assertThat(FileUrls.normalize("card/2026/10/a.jpg")).isEqualTo("card/2026/10/a.jpg");
        assertThat(FileUrls.normalize("https://cdn.example.com/a.jpg")).isEqualTo("https://cdn.example.com/a.jpg");
        assertThat(FileUrls.normalize(null)).isEmpty();
    }
}
