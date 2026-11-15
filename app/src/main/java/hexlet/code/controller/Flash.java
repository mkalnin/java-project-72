package hexlet.code.controller;

import io.javalin.http.Context;

import java.util.Map;

public final class Flash {
    private Flash() {
    }

    public static void set(Context ctx, String message, String type) {
        ctx.sessionAttribute("flash", message);
        ctx.sessionAttribute("flashType", type);
    }

    public static void put(Context ctx, Map<String, Object> model) {
        model.put("flash", ctx.sessionAttribute("flash"));
        model.put("flashType", ctx.sessionAttribute("flashType"));
        ctx.consumeSessionAttribute("flash");
        ctx.consumeSessionAttribute("flashType");
    }
}
