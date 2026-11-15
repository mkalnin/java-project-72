package hexlet.code;

import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class UrlCheckControllerTest extends AppTest {
    private static MockWebServer mockServer;

    @BeforeAll
    static void startServer() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        mockServer.shutdown();
    }

    private UrlRepository urlRepository() {
        return new UrlRepository(Database.getDataSource());
    }

    private UrlCheckRepository urlCheckRepository() {
        return new UrlCheckRepository(Database.getDataSource());
    }

    private String serverUrl() {
        return mockServer.url("/").toString();
    }

    @Test
    void testCreateCheck() {
        mockServer.enqueue(new MockResponse()
                .setBody("<html><head><title>Test title</title>"
                        + "<meta name=\"description\" content=\"Test description\"></head>"
                        + "<body><h1>Test h1</h1></body></html>")
                .setResponseCode(200));

        runTest((server, client) -> {
            client.post("/urls", "url=" + serverUrl());
            var url = urlRepository().findAll().get(0);

            var response = client.post("/urls/" + url.getId() + "/checks");
            assertThat(response.code()).isEqualTo(302);

            var checks = urlCheckRepository().findByUrlId(url.getId());
            assertThat(checks).hasSize(1);
            var check = checks.get(0);
            assertThat(check.getStatusCode()).isEqualTo(200);
            assertThat(check.getTitle()).isEqualTo("Test title");
            assertThat(check.getH1()).isEqualTo("Test h1");
            assertThat(check.getDescription()).isEqualTo("Test description");
            assertThat(check.getUrlId()).isEqualTo(url.getId());
            assertThat(check.getCreatedAt()).isNotNull();

            var showResponse = client.get("/urls/" + url.getId());
            var body = showResponse.body().string();
            assertThat(body).contains("data-test=\"checks\"");
            assertThat(body).contains("200");
        });
    }

    @Test
    void testCreateCheckWithErrorStatus() {
        mockServer.enqueue(new MockResponse().setResponseCode(500));

        runTest((server, client) -> {
            client.post("/urls", "url=" + serverUrl());
            var url = urlRepository().findAll().get(0);

            var response = client.post("/urls/" + url.getId() + "/checks");
            assertThat(response.code()).isEqualTo(302);

            assertThat(urlCheckRepository().findByUrlId(url.getId())).isEmpty();
        });
    }

    @Test
    void testCreateCheckWithNotFoundStatus() {
        mockServer.enqueue(new MockResponse().setResponseCode(404));

        runTest((server, client) -> {
            client.post("/urls", "url=" + serverUrl());
            var url = urlRepository().findAll().get(0);

            client.post("/urls/" + url.getId() + "/checks");

            assertThat(urlCheckRepository().findByUrlId(url.getId())).isEmpty();
        });
    }

    @Test
    void testFlashMessageAfterCheck() {
        mockServer.enqueue(new MockResponse()
                .setBody("<html><head><title>Title</title></head><body></body></html>")
                .setResponseCode(200));

        runTest((server, client) -> {
            var createResponse = client.post("/urls", "url=" + serverUrl());
            var cookie = createResponse.headers().get("Set-Cookie").get(0).split(";")[0];
            var url = urlRepository().findAll().get(0);

            client.post("/urls/" + url.getId() + "/checks", "", request -> request.header("Cookie", cookie));

            var showResponse = client.get("/urls/" + url.getId(), request -> request.header("Cookie", cookie));
            assertThat(showResponse.body().string()).contains("Страница успешно проверена");
        });
    }

    @Test
    void testFlashMessageOnCheckError() {
        mockServer.enqueue(new MockResponse().setResponseCode(500));

        runTest((server, client) -> {
            var createResponse = client.post("/urls", "url=" + serverUrl());
            var cookie = createResponse.headers().get("Set-Cookie").get(0).split(";")[0];
            var url = urlRepository().findAll().get(0);

            client.post("/urls/" + url.getId() + "/checks", "", request -> request.header("Cookie", cookie));

            var showResponse = client.get("/urls/" + url.getId(), request -> request.header("Cookie", cookie));
            assertThat(showResponse.body().string()).contains("Произошла ошибка при проверке");
        });
    }

    @Test
    void testCheckMissingUrl() {
        runTest((server, client) -> {
            var response = client.post("/urls/999/checks");
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    void testUrlsListShowsLastCheck() {
        mockServer.enqueue(new MockResponse()
                .setBody("<html><head><title>Title</title></head><body></body></html>")
                .setResponseCode(200));

        runTest((server, client) -> {
            client.post("/urls", "url=" + serverUrl());
            var url = urlRepository().findAll().get(0);
            client.post("/urls/" + url.getId() + "/checks");

            var body = client.get("/urls").body().string();
            assertThat(body).contains("data-test=\"urls\"");
            assertThat(body).contains("200");
        });
    }
}
