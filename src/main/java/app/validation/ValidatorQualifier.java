package app.validation;

import jakarta.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
public @interface ValidatorQualifier {

    ValidationChoice value();

    public enum ValidationChoice {
        BOOK, RETURN, BORROW, FINE, RESERVATION
    }
}