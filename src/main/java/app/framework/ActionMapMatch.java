package app.framework;

import java.lang.reflect.Method;

public class ActionMapMatch {
    private Object action;
    private Method method;

    public ActionMapMatch(Object action, Method method) {
        this.action = action;
        this.method = method;
    }

    public Object getAction() { return action; }
    public Method getMethod() { return method; }
}