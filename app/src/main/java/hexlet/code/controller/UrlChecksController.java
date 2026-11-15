package hexlet.code.controller;

import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;

import java.sql.SQLException;
import java.util.HashMap;

public class UrlChecksController {
    private static final int MAX_TEXT_LENGTH = 200;

    private final UrlRepository urlRepository;
    private final UrlCheckRepository urlCheckRepository;

    public UrlChecksController(UrlRepository urlRepository, UrlCheckRepository urlCheckRepository) {
        this.urlRepository = urlRepository;
        this.urlCheckRepository = urlCheckRepository;
    }

    public void create(Context ctx) throws SQLException {
        var id = ctx.pathParamAsClass("id", Long.class).get();
        var url = urlRepository.findById(id).orElse(null);
        if (url == null) {
            ctx.status(HttpStatus.NOT_FOUND);
            var model = new HashMap<String, Object>();
            Flash.put(ctx, model);
            ctx.render("errors/404.jte", model);
            return;
        }

        try {
            var response = Unirest.get(url.getName()).asString();
            var statusCode = response.getStatus();
            if (statusCode >= 400) {
                Flash.set(ctx, "Произошла ошибка при проверке", "error");
                ctx.redirect("/urls/" + id);
                return;
            }

            var document = Jsoup.parse(response.getBody());
            var title = document.title();
            var h1Element = document.selectFirst("h1");
            String h1 = h1Element == null ? null : h1Element.text();
            var descriptionElement = document.selectFirst("meta[name=description]");
            String description = descriptionElement == null ? null : descriptionElement.attr("content");

            var check = new UrlCheck(id, statusCode, title, h1, description);
            urlCheckRepository.save(check);

            Flash.set(ctx, "Страница успешно проверена", "success");
        } catch (Exception e) {
            Flash.set(ctx, "Произошла ошибка при проверке", "error");
        }
        ctx.redirect("/urls/" + id);
    }

    public static String truncate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() > MAX_TEXT_LENGTH
                ? value.substring(0, MAX_TEXT_LENGTH) + "..."
                : value;
    }
}
