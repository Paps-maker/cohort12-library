package app.framework;

import jakarta.inject.Qualifier;
import jakarta.enterprise.context.RequestScoped;
import java.lang.annotation.*;

/**
 * Custom CDI Qualifier used to identify and discover Web Controller components
 * dynamically at system startup.
 */
@Qualifier
@RequestScoped // Automatically handles instantiation per HTTP request lifecycle
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Controller {
}