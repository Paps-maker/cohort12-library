package app.listener;

import java.util.HashMap;
import java.util.Map;

public class ServletRegistry {

    // URL → Servlet Class
    public static Map<String, Class<?>> routes = new HashMap<>();

    static {



    }

    public static Class<?> getServlet(String path) {
        return routes.get(path);
    }
}