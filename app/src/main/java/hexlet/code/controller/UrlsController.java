package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class UrlsController {
    private final UrlRepository urlRepository;
    private final UrlCheckRepository urlCheckRepository;

    public UrlsController(UrlRepository urlRepository, UrlCheckRepository urlCheckRepository) {
        this.urlRepository = urlRepository;
        this.urlCheckRepository = urlCheckRepository;
    }

    public void index(Context ctx) throws SQLException {
        var lastChecks = urlCheckRepository.findLastChecks();
        var lastCheckDates = new HashMap<Long, String>();
        var lastCheckStatuses = new HashMap<Long, String>();
        lastChecks.forEach((urlId, check) -> {
            lastCheckDates.put(urlId, check.getCreatedAt().toLocalDateTime().toLocalDate().toString());
            lastCheckStatuses.put(urlId, check.getStatusCode().toString());
        });

        var model = new HashMap<String, Object>();
        model.put("urls", urlRepository.findAll());
        model.put("lastCheckDates", lastCheckDates);
        model.put("lastCheckStatuses", lastCheckStatuses);
        Flash.put(ctx, model);
        ctx.render("urls/index.jte", model);
    }

    public void newForm(Context ctx) {
        var model = new HashMap<String, Object>();
        model.put("input", "");
        Flash.put(ctx, model);
        ctx.render("urls/new.jte", model);
    }

    public void show(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = urlRepository.findById(id).orElse(null);
        if (url == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            var model = new HashMap<String, Object>();
            Flash.put(ctx, model);
            ctx.render("errors/404.jte", model);
            return;
        }
        var model = new HashMap<String, Object>();
        model.put("url", url);
        model.put("checks", urlCheckRepository.findByUrlId(id));
        Flash.put(ctx, model);
        ctx.render("urls/show.jte", model);
    }

    public void create(Context ctx) throws SQLException {
        var input = ctx.formParam("url");
        var normalized = normalize(input);

        if (normalized == null) {
            ctx.status(HttpStatus.UNPROCESSABLE_CONTENT);
            var model = new HashMap<String, Object>();
            model.put("flash", "Некорректный URL");
            model.put("flashType", "error");
            model.put("input", input == null ? "" : input);
            ctx.render("urls/new.jte", model);
            return;
        }

        var existing = urlRepository.findByName(normalized);
        if (existing.isPresent()) {
            Flash.set(ctx, "Страница уже существует", "info");
            ctx.redirect("/urls/" + existing.get().getId());
            return;
        }

        var url = new Url(normalized);
        urlRepository.save(url);
        Flash.set(ctx, "Страница успешно добавлена", "success");
        ctx.redirect("/urls/" + url.getId());
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
