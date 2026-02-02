package org.example.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserEntity Tests")
class UserEntityTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create UserEntity with default constructor")
    void testDefaultConstructor() {
        // Act
        UserEntity user = new UserEntity();

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isNull();
        assertThat(user.getPassword()).isNull();
    }

    @Test
    @DisplayName("Should create UserEntity with parameterized constructor")
    void testParameterizedConstructor() {
        // Act
        UserEntity user = new UserEntity("test@example.com", "password123");

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPassword()).isEqualTo("password123");
        assertThat(user.getId()).isNull(); // ID is auto-generated, so it's null before persistence
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testGettersAndSetters() {
        // Arrange
        UserEntity user = new UserEntity();

        // Act
        user.setId(1L);
        user.setEmail("setter@example.com");
        user.setPassword("setterPassword");

        // Assert
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("setter@example.com");
        assertThat(user.getPassword()).isEqualTo("setterPassword");
    }

    @Test
    @DisplayName("Should pass validation with valid email")
    void testEmailValidation_Valid() {
        // Arrange
        UserEntity user = new UserEntity("valid@example.com", "password123");

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should fail validation with invalid email format")
    void testEmailValidation_InvalidFormat() {
        // Arrange
        UserEntity user = new UserEntity("invalid-email", "password123");

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Email should be valid"));
    }

    @Test
    @DisplayName("Should fail validation with blank email")
    void testEmailValidation_Blank() {
        // Arrange
        UserEntity user = new UserEntity("", "password123");

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Email is required"));
    }

    @Test
    @DisplayName("Should fail validation with null email")
    void testEmailValidation_Null() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setPassword("password123");

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @Test
    @DisplayName("Should fail validation with blank password")
    void testPasswordValidation_Blank() {
        // Arrange
        UserEntity user = new UserEntity("test@example.com", "");

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().contains("Password is required"));
    }

    @Test
    @DisplayName("Should fail validation with null password")
    void testPasswordValidation_Null() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
    }

    @Test
    @DisplayName("Should pass validation with valid email and password")
    void testValidation_AllFieldsValid() {
        // Arrange
        UserEntity user = new UserEntity("user@example.com", "securePassword123");

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should handle long email within limit")
    void testEmailValidation_LongEmailWithinLimit() {
        // Arrange - Email column has length 100 but @Email validator has stricter rules
        // Use a more realistic long email
        String longEmail = "very.long.email.address.with.multiple.dots@example-domain.com"; // ~60 chars
        UserEntity user = new UserEntity(longEmail, "password123");

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void testPasswordValidation_SpecialCharacters() {
        // Arrange
        UserEntity user = new UserEntity("test@example.com", "P@ssw0rd!#$%");

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should handle whitespace trimming in email")
    void testEmailValidation_Whitespace() {
        // Arrange - Testing with leading/trailing whitespace
        UserEntity user = new UserEntity(" test@example.com ", "password123");

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        // Validation should still work, but whitespace in email is not removed by @Email
        assertThat(user.getEmail()).isEqualTo(" test@example.com ");
    }

    @Test
    @DisplayName("Should validate email with various valid formats")
    void testEmailValidation_VariousFormats() {
        // Test different valid email formats
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk",
            "user_name@example.org"
        };

        for (String email : validEmails) {
            // Arrange
            UserEntity user = new UserEntity(email, "password123");

            // Act
            Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

            // Assert
            assertThat(violations)
                .withFailMessage("Email format should be valid: " + email)
                .isEmpty();
        }
    }
}
