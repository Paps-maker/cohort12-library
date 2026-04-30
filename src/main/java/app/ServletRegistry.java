package app;

import app.borrow;

import java.util.HashMap;
import java.util.Map;

public class ServletRegistry {

    // URL → Servlet Class
    public static Map<String, Class<?>> routes = new HashMap<>();

    static {
        routes.put("/books", BookServlet.class);
        routes.put("/members", members.class);
        routes.put("/borrow", borrow.class);
    }

    public static Class<?> getServlet(String path) {
        return routes.get(path);
    }
}