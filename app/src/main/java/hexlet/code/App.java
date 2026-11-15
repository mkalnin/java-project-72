package hexlet.code;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.resolve.DirectoryCodeResolver;
import gg.jte.resolve.ResourceCodeResolver;
import hexlet.code.controller.UrlController;
import hexlet.code.repository.UrlRepository;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinJte;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class App {
    private static final Path TEMPLATES_PATH = Path.of("src", "main", "resources", "templates");
    private static final Path STATIC_PATH = Path.of("src", "main", "resources", "static");
    private static final Path JTE_CLASSES_PATH = Path.of("jte-classes");

    public static void main(String[] args) throws SQLException, IOException {
        Database.reset();
        var app = getApp();
        app.start(getPort());
    }

    public static Javalin getApp() {
        var urlRepository = new UrlRepository(Database.getDataSource());
        var urlController = new UrlController(urlRepository);

        return Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.fileRenderer(new JavalinJte(createTemplateEngine()));

            if (isDevelopment()) {
                config.staticFiles.add(STATIC_PATH.toString(), Location.EXTERNAL);
            } else {
                config.staticFiles.add("/static", Location.CLASSPATH);
            }

            config.routes.get("/", ctx -> {
                var model = new HashMap<String, Object>();
                model.put("flash", ctx.sessionAttribute("flash"));
                model.put("flashType", ctx.sessionAttribute("flashType"));
                ctx.consumeSessionAttribute("flash");
                ctx.consumeSessionAttribute("flashType");
                ctx.render("index.jte", model);
            });
            config.routes.post("/urls", urlController::create);
            config.routes.get("/urls", urlController::index);
            config.routes.get("/urls/{id}", urlController::show);
        });
    }

    private static TemplateEngine createTemplateEngine() {
        if (isDevelopment()) {
            var codeResolver = new DirectoryCodeResolver(TEMPLATES_PATH);
            return TemplateEngine.create(codeResolver, JTE_CLASSES_PATH, ContentType.Html);
        }
        var classLoader = App.class.getClassLoader();
        var codeResolver = new ResourceCodeResolver("templates", classLoader);
        return TemplateEngine.create(codeResolver, ContentType.Html);
    }

    private static String getMode() {
        return System.getenv().getOrDefault("APP_ENV", "production");
    }

    private static boolean isDevelopment() {
        return getMode().equals("development") && Files.isDirectory(TEMPLATES_PATH);
    }

    private static int getPort() {
        var port = System.getenv("PORT");
        return port == null ? 7070 : Integer.parseInt(port);
    }
}
