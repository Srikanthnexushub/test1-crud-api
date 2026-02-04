package org.example.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginRequest DTO Tests")
class LoginRequestTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should create LoginRequest with parameterized constructor")
    void testParameterizedConstructor() {
        // Act
        LoginRequest request = new LoginRequest("test@example.com", "password123");

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.email()).isEqualTo("test@example.com");
        assertThat(request.password()).isEqualTo("password123");
    }

    @Test
    @DisplayName("Should pass validation with valid data")
    void testValidationWithValidData() {
        // Arrange
        LoginRequest request = new LoginRequest("valid@example.com", "password123");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation with null email")
    void testValidationWithNullEmail() {
        // Arrange
        LoginRequest request = new LoginRequest(null, "password123");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("email") &&
            v.getMessage().contains("Email is required")
        );
    }

    @Test
    @DisplayName("Should fail validation with blank email")
    void testValidationWithBlankEmail() {
        // Arrange
        LoginRequest request = new LoginRequest("", "password123");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Email is required"));
    }

    @Test
    @DisplayName("Should fail validation with invalid email format")
    void testValidationWithInvalidEmail() {
        // Arrange
        LoginRequest request = new LoginRequest("invalid-email", "password123");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Email should be valid"));
    }

    @Test
    @DisplayName("Should fail validation with null password")
    void testValidationWithNullPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", null);

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("password") &&
            v.getMessage().contains("Password is required")
        );
    }

    @Test
    @DisplayName("Should fail validation with blank password")
    void testValidationWithBlankPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Password is required"));
    }

    @Test
    @DisplayName("Should fail validation with too short password")
    void testValidationWithShortPassword() {
        // Arrange - Password must be at least 6 characters
        LoginRequest request = new LoginRequest("test@example.com", "abc12");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getMessage().contains("Password must be between 6 and 100 characters")
        );
    }

    @Test
    @DisplayName("Should pass validation with minimum password length")
    void testValidationWithMinimumPasswordLength() {
        // Arrange - Exactly 6 characters
        LoginRequest request = new LoginRequest("test@example.com", "pass12");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation with too long password")
    void testValidationWithTooLongPassword() {
        // Arrange - More than 100 characters
        String longPassword = "a".repeat(101);
        LoginRequest request = new LoginRequest("test@example.com", longPassword);

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getMessage().contains("Password must be between 6 and 100 characters")
        );
    }

    @Test
    @DisplayName("Should pass validation with maximum password length")
    void testValidationWithMaximumPasswordLength() {
        // Arrange - Exactly 100 characters
        String maxPassword = "a".repeat(100);
        LoginRequest request = new LoginRequest("test@example.com", maxPassword);

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should serialize to JSON correctly")
    void testJsonSerialization() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("json@example.com", "jsonPassword");

        // Act
        String json = objectMapper.writeValueAsString(request);

        // Assert
        assertThat(json).isNotNull();
        assertThat(json).contains("json@example.com");
        assertThat(json).contains("jsonPassword");
        assertThat(json).contains("\"email\"");
        assertThat(json).contains("\"password\"");
    }

    @Test
    @DisplayName("Should deserialize from JSON correctly")
    void testJsonDeserialization() throws Exception {
        // Arrange
        String json = "{\"email\":\"deserialize@example.com\",\"password\":\"deserializePass\"}";

        // Act
        LoginRequest request = objectMapper.readValue(json, LoginRequest.class);

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.email()).isEqualTo("deserialize@example.com");
        assertThat(request.password()).isEqualTo("deserializePass");
    }

    @Test
    @DisplayName("Should handle JSON with missing fields")
    void testJsonDeserializationWithMissingFields() throws Exception {
        // Arrange
        String json = "{\"email\":\"partial@example.com\"}";

        // Act
        LoginRequest request = objectMapper.readValue(json, LoginRequest.class);

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.email()).isEqualTo("partial@example.com");
        assertThat(request.password()).isNull();
    }

    @Test
    @DisplayName("Should handle JSON with null values")
    void testJsonDeserializationWithNullValues() throws Exception {
        // Arrange
        String json = "{\"email\":null,\"password\":null}";

        // Act
        LoginRequest request = objectMapper.readValue(json, LoginRequest.class);

        // Assert
        assertThat(request).isNotNull();
        assertThat(request.email()).isNull();
        assertThat(request.password()).isNull();
    }

    @Test
    @DisplayName("Should validate email with special characters")
    void testValidationWithSpecialCharactersInEmail() {
        // Arrange
        LoginRequest request = new LoginRequest("user+tag@example.com", "password123");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should validate password with special characters")
    void testValidationWithSpecialCharactersInPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "P@ssw0rd!#$%");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation with whitespace-only email")
    void testValidationWithWhitespaceOnlyEmail() {
        // Arrange
        LoginRequest request = new LoginRequest("   ", "password123");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should fail validation with whitespace-only password")
    void testValidationWithWhitespaceOnlyPassword() {
        // Arrange
        LoginRequest request = new LoginRequest("test@example.com", "      ");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should fail validation with multiple constraint violations")
    void testValidationWithMultipleViolations() {
        // Arrange - Both email and password are invalid
        LoginRequest request = new LoginRequest("invalid-email", "abc");

        // Act
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        // Assert
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }
}
