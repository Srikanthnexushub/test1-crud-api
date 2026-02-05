package org.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validates that a string does not contain HTML tags or script content.
 */
public class NoHtmlValidator implements ConstraintValidator<NoHtml, String> {

    private static final Pattern HTML_PATTERN = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE);
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
            "(javascript:|on\\w+\\s*=|<\\s*script)", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return true; // null/empty handled by @NotBlank
        }
        return !HTML_PATTERN.matcher(value).find() && !SCRIPT_PATTERN.matcher(value).find();
    }
}
