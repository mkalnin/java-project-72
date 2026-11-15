package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UrlController {
    private final UrlRepository urlRepository;

    public UrlController(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    public void index(Context ctx) throws SQLException {
        var model = new HashMap<String, Object>();
        model.put("urls", urlRepository.findAll());
        putFlash(ctx, model);
        ctx.render("urls/index.jte", model);
    }

    public void show(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = urlRepository.findById(id).orElse(null);
        if (url == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            var model = new HashMap<String, Object>();
            putFlash(ctx, model);
            ctx.render("errors/404.jte", model);
            return;
        }
        var model = new HashMap<String, Object>();
        model.put("url", url);
        putFlash(ctx, model);
        ctx.render("urls/show.jte", model);
    }

    public void create(Context ctx) throws SQLException {
        var input = ctx.formParam("url");
        var normalized = normalize(input);

        if (normalized == null) {
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);
            ctx.render("index.jte", Map.of(
                    "flash", "Некорректный URL",
                    "flashType", "error",
                    "input", input == null ? "" : input
            ));
            return;
        }

        var existing = urlRepository.findByName(normalized);
        if (existing.isPresent()) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flashType", "info");
            ctx.redirect("/urls/" + existing.get().getId());
            return;
        }

        var url = new Url(normalized);
        urlRepository.save(url);
        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flashType", "success");
        ctx.redirect("/urls/" + url.getId());
    }

    private static void putFlash(Context ctx, Map<String, Object> model) {
        model.put("flash", ctx.sessionAttribute("flash"));
        model.put("flashType", ctx.sessionAttribute("flashType"));
        ctx.consumeSessionAttribute("flash");
        ctx.consumeSessionAttribute("flashType");
    }

    private static String normalize(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }
        try {
            var uri = new URI(input.trim());
            var url = uri.toURL();
            var scheme = url.getProtocol();
            if (scheme == null || !(scheme.equals("http") || scheme.equals("https"))) {
                return null;
            }
            var host = url.getHost();
            if (host == null || host.isBlank()) {
                return null;
            }
            var port = url.getPort();
            return port == -1
                    ? scheme + "://" + host
                    : scheme + "://" + host + ":" + port;
        } catch (URISyntaxException | IllegalArgumentException | MalformedURLException e) {
            return null;
        }
    }
}
