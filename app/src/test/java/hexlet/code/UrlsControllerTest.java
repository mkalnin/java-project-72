package hexlet.code;

import hexlet.code.repository.UrlRepository;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UrlsControllerTest extends AppTest {

    private UrlRepository urlRepository() {
        return new UrlRepository(Database.getDataSource());
    }

    @Test
    void testIndex() {
        runTest((server, client) -> {
            client.post("/urls", "url=https://example.com");
            client.post("/urls", "url=https://hexlet.io");

            var response = client.get("/urls");
            assertThat(response.code()).isEqualTo(200);
            var body = response.body().string();
            assertThat(body).contains("data-test=\"urls\"");
            assertThat(body).contains("https://example.com");
            assertThat(body).contains("https://hexlet.io");
        });
    }

    @Test
    void testIndexShowsNewestFirst() {
        runTest((server, client) -> {
            client.post("/urls", "url=https://first.com");
            client.post("/urls", "url=https://second.com");

            var body = client.get("/urls").body().string();
            assertThat(body.indexOf("https://second.com"))
                    .isLessThan(body.indexOf("https://first.com"));
        });
    }

    @Test
    void testNew() {
        runTest((server, client) -> {
            var response = client.get("/urls/new");
            assertThat(response.code()).isEqualTo(200);
            var body = response.body().string();
            assertThat(body).contains("name=\"url\"");
            assertThat(body).contains("action=\"/urls\"");
            assertThat(body).contains("Проверить");
        });
    }

    @Test
    void testCreate() {
        runTest((server, client) -> {
            var response = client.post("/urls", "url=https://example.com/path");
            assertThat(response.code()).isEqualTo(302);

            var urls = urlRepository().findAll();
            assertThat(urls).hasSize(1);
            var url = urls.get(0);
            assertThat(url.getName()).isEqualTo("https://example.com");
            assertThat(url.getId()).isNotNull();
            assertThat(url.getCreatedAt()).isNotNull();
        });
    }

    @Test
    void testCreateNormalizesPort() {
        runTest((server, client) -> {
            client.post("/urls", "url=https://example.com:8080/path");

            var urls = urlRepository().findAll();
            assertThat(urls).hasSize(1);
            assertThat(urls.get(0).getName()).isEqualTo("https://example.com:8080");
        });
    }

    @Test
    void testCreateDuplicate() {
        runTest((server, client) -> {
            client.post("/urls", "url=https://example.com");
            var response = client.post("/urls", "url=https://example.com/other");

            assertThat(response.code()).isEqualTo(302);
            assertThat(urlRepository().findAll()).hasSize(1);
        });
    }

    @Test
    void testCreateInvalid() {
        runTest((server, client) -> {
            var response = client.post("/urls", "url=httpsss://abcabca@test.ru");
            assertThat(response.code()).isEqualTo(422);
            assertThat(response.body().string()).contains("Некорректный URL");
            assertThat(urlRepository().findAll()).isEmpty();
        });
    }

    @Test
    void testCreateEmpty() {
        runTest((server, client) -> {
            var response = client.post("/urls", "url=");
            assertThat(response.code()).isEqualTo(422);
            assertThat(urlRepository().findAll()).isEmpty();
        });
    }

    @Test
    void testShow() {
        runTest((server, client) -> {
            client.post("/urls", "url=https://example.com");
            var url = urlRepository().findAll().get(0);

            var response = client.get("/urls/" + url.getId());
            assertThat(response.code()).isEqualTo(200);
            var body = response.body().string();
            assertThat(body).contains("data-test=\"url\"");
            assertThat(body).contains("data-test=\"checks\"");
            assertThat(body).contains("https://example.com");
            assertThat(body).contains("Запустить проверку");
        });
    }

    @Test
    void testShowMissing() {
        runTest((server, client) -> {
            var response = client.get("/urls/999");
            assertThat(response.code()).isEqualTo(404);
        });
    }
}
