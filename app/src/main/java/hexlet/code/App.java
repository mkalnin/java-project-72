package hexlet.code;

import io.javalin.Javalin;

import java.io.IOException;
import java.sql.SQLException;

public class App {
    public static void main(String[] args) throws SQLException, IOException {
        Database.reset();
        var app = getApp();
        app.start(getPort());
    }

    public static Javalin getApp() {
        return Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.routes.get("/", ctx -> ctx.result("Hello World"));
        });
    }

    private static int getPort() {
        var port = System.getenv("PORT");
        return port == null ? 7070 : Integer.parseInt(port);
    }
}
