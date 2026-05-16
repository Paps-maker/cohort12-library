package app.framework;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ActionMap {
    private final Map<String, ActionMapMatch> routes = new HashMap<>();

    public void register(Object actionInstance) {
        Method[] methods = actionInstance.getClass().getDeclaredMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(ActionGetMethod.class)) {
                String path = m.getAnnotation(ActionGetMethod.class).value();
                routes.put("GET:" + path, new ActionMapMatch(actionInstance, m));
            } else if (m.isAnnotationPresent(ActionPostMethod.class)) {
                String path = m.getAnnotation(ActionPostMethod.class).value();
                routes.put("POST:" + path, new ActionMapMatch(actionInstance, m));
            }
        }
    }

    public ActionMapMatch find(String verb, String path) {
        return routes.get(verb.toUpperCase() + ":" + path);
    }
}