package hexlet.code;

import hexlet.code.repository.UrlRepository;
import io.javalin.testtools.JavalinTest;
import io.javalin.testtools.TestConfig;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static org.assertj.core.api.Assertions.assertThat;

class UrlControllerTest extends AppTest {

    private UrlRepository urlRepository() {
        return new UrlRepository(Database.getDataSource());
    }

    @Test
    void testCreateUrl() {
        runTest((server, client) -> {
            var response = client.post("/urls", "url=https://example.com/path");
            assertThat(response.code()).isEqualTo(302);

            var urls = urlRepository().findAll();
            assertThat(urls).hasSize(1);
            var url = urls.get(0);
            assertThat(url.getName()).isEqualTo("https://example.com");
            assertThat(url.getId()).isNotNull();
            assertThat(url.getCreatedAt()).isNotNull();

            var showResponse = client.get("/urls/" + url.getId());
            assertThat(showResponse.code()).isEqualTo(200);
            var body = showResponse.body().string();
            assertThat(body).contains("data-test=\"url\"");
            assertThat(body).contains("https://example.com");
        });
    }

    @Test
    void testCreateUrlWithPort() {
        runTest((server, client) -> {
            client.post("/urls", "url=https://example.com:8080/path");

            var urls = urlRepository().findAll();
            assertThat(urls).hasSize(1);
            assertThat(urls.get(0).getName()).isEqualTo("https://example.com:8080");
        });
    }

    @Test
    void testCreateDuplicateUrl() {
        runTest((server, client) -> {
            client.post("/urls", "url=https://example.com");
            var response = client.post("/urls", "url=https://example.com/other");

            assertThat(response.code()).isEqualTo(302);

            var urls = urlRepository().findAll();
            assertThat(urls).hasSize(1);
            var url = urls.get(0);

            var showResponse = client.get("/urls/" + url.getId());
            assertThat(showResponse.code()).isEqualTo(200);
            assertThat(showResponse.body().string()).contains("https://example.com");
        });
    }

    @Test
    void testCreateInvalidUrl() {
        runTest((server, client) -> {
            var response = client.post("/urls", "url=not a url");
            assertThat(response.code()).isEqualTo(422);
            assertThat(response.body().string()).contains("Некорректный URL");
            assertThat(urlRepository().findAll()).isEmpty();
        });
    }

    @Test
    void testCreateEmptyUrl() {
        runTest((server, client) -> {
            var response = client.post("/urls", "url=");
            assertThat(response.code()).isEqualTo(422);
            assertThat(urlRepository().findAll()).isEmpty();
        });
    }

    @Test
    void testUrlsList() {
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
    void testUrlsListSortedByCreatedAtDesc() {
        runTest((server, client) -> {
            client.post("/urls", "url=https://first.com");
            client.post("/urls", "url=https://second.com");

            var body = client.get("/urls").body().string();
            assertThat(body.indexOf("https://second.com"))
                    .isLessThan(body.indexOf("https://first.com"));
        });
    }

    @Test
    void testShowMissingUrl() {
        runTest((server, client) -> {
            var response = client.get("/urls/999");
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    void testRedirectFollowedToShowPage() {
        var config = new TestConfig(
                true, true, HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build());
        JavalinTest.test(app, config, (server, client) -> {
            var response = client.post("/urls", "url=https://example.com");
            assertThat(response.code()).isEqualTo(200);
            assertThat(response.body().string()).contains("data-test=\"url\"");
        });
    }

    @Test
    void testFlashMessageAfterCreate() {
        runTest((server, client) -> {
            var response = client.post("/urls", "url=https://example.com");
            var cookie = response.headers().get("Set-Cookie").get(0).split(";")[0];

            var showResponse = client.get("/urls/1", request -> request.header("Cookie", cookie));
            assertThat(showResponse.body().string()).contains("Страница успешно добавлена");
        });
    }

    @Test
    void testFlashMessageForDuplicate() {
        runTest((server, client) -> {
            var firstResponse = client.post("/urls", "url=https://example.com");
            var cookie = firstResponse.headers().get("Set-Cookie").get(0).split(";")[0];

            var response = client.post("/urls", "url=https://example.com/other",
                    request -> request.header("Cookie", cookie));
            assertThat(response.code()).isEqualTo(302);

            var showResponse = client.get("/urls/1", request -> request.header("Cookie", cookie));
            assertThat(showResponse.body().string()).contains("Страница уже существует");
        });
    }
}
