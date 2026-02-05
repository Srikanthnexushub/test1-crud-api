package org.example.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validation annotation that rejects input containing HTML tags or script content.
 */
@Documented
@Constraint(validatedBy = NoHtmlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoHtml {
    String message() default "Input must not contain HTML or script tags";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
