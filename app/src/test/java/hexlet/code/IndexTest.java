package hexlet.code;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IndexTest extends AppTest {

    @Test
    void testIndexPage() {
        runTest((server, client) -> {
            var response = client.get("/");
            assertThat(response.code()).isEqualTo(200);
            var body = response.body().string();
            assertThat(body).contains("Анализатор страниц");
            assertThat(body).contains("name=\"url\"");
            assertThat(body).contains("action=\"/urls\"");
        });
    }
}
