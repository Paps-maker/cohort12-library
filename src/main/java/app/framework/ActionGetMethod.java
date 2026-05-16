package app.framework;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ActionGetMethod {
    String value(); // This will hold the URL path like "/fines"
}