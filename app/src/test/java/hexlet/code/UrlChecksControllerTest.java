package hexlet.code;

import hexlet.code.controller.UrlChecksController;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class UrlChecksControllerTest extends AppTest {
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
    void testCreate() {
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
        });
    }

    @Test
    void testCreateShowsCheckOnUrlPage() {
        mockServer.enqueue(new MockResponse()
                .setBody("<html><head><title>Test title</title>"
                        + "<meta name=\"description\" content=\"Test description\"></head>"
                        + "<body><h1>Test h1</h1></body></html>")
                .setResponseCode(200));

        runTest((server, client) -> {
            client.post("/urls", "url=" + serverUrl());
            var url = urlRepository().findAll().get(0);
            client.post("/urls/" + url.getId() + "/checks");

            var body = client.get("/urls/" + url.getId()).body().string();
            assertThat(body).contains("data-test=\"checks\"");
            assertThat(body).contains("200");
            assertThat(body).contains("Test h1");
            assertThat(body).contains("Test title");
            assertThat(body).contains("Test description");
        });
    }

    @Test
    void testCreateWithServerError() {
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
    void testCreateWithNotFound() {
        mockServer.enqueue(new MockResponse().setResponseCode(404));

        runTest((server, client) -> {
            client.post("/urls", "url=" + serverUrl());
            var url = urlRepository().findAll().get(0);

            client.post("/urls/" + url.getId() + "/checks");
            assertThat(urlCheckRepository().findByUrlId(url.getId())).isEmpty();
        });
    }

    @Test
    void testCreateMissingUrl() {
        runTest((server, client) -> {
            var response = client.post("/urls/999/checks");
            assertThat(response.code()).isEqualTo(404);
        });
    }

    @Test
    void testCreateShowsSuccessFlash() {
        mockServer.enqueue(new MockResponse()
                .setBody("<html><head><title>Title</title></head><body></body></html>")
                .setResponseCode(200));

        runTest((server, client) -> {
            var createResponse = client.post("/urls", "url=" + serverUrl());
            var cookie = createResponse.headers().get("Set-Cookie").get(0).split(";")[0];
            var url = urlRepository().findAll().get(0);

            client.post("/urls/" + url.getId() + "/checks", "", request -> request.header("Cookie", cookie));

            var body = client.get("/urls/" + url.getId(), request -> request.header("Cookie", cookie))
                    .body().string();
            assertThat(body).contains("Страница успешно проверена");
        });
    }

    @Test
    void testCreateShowsErrorFlash() {
        mockServer.enqueue(new MockResponse().setResponseCode(500));

        runTest((server, client) -> {
            var createResponse = client.post("/urls", "url=" + serverUrl());
            var cookie = createResponse.headers().get("Set-Cookie").get(0).split(";")[0];
            var url = urlRepository().findAll().get(0);

            client.post("/urls/" + url.getId() + "/checks", "", request -> request.header("Cookie", cookie));

            var body = client.get("/urls/" + url.getId(), request -> request.header("Cookie", cookie))
                    .body().string();
            assertThat(body).contains("Произошла ошибка при проверке");
        });
    }

    @Test
    void testLongValuesAreTruncated() {
        var longText = "a".repeat(250);
        mockServer.enqueue(new MockResponse()
                .setBody("<html><head><title>" + longText + "</title>"
                        + "<meta name=\"description\" content=\"" + longText + "\"></head>"
                        + "<body><h1>" + longText + "</h1></body></html>")
                .setResponseCode(200));

        runTest((server, client) -> {
            client.post("/urls", "url=" + serverUrl());
            var url = urlRepository().findAll().get(0);
            client.post("/urls/" + url.getId() + "/checks");

            var body = client.get("/urls/" + url.getId()).body().string();
            assertThat(body).contains("a".repeat(200) + "...");
            assertThat(body).doesNotContain("a".repeat(201));
        });
    }

    @Test
    void testTruncate() {
        assertThat(UrlChecksController.truncate(null)).isEmpty();
        assertThat(UrlChecksController.truncate("short")).isEqualTo("short");
        assertThat(UrlChecksController.truncate("b".repeat(200))).isEqualTo("b".repeat(200));
        assertThat(UrlChecksController.truncate("c".repeat(201))).isEqualTo("c".repeat(200) + "...");
    }
}
