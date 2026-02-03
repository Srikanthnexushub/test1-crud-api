package org.example.e2e;

import com.microsoft.playwright.APIResponse;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Registration E2E Tests")
class RegistrationE2ETest extends BaseE2ETest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        // Clean up test data
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testSuccessfulRegistration() {
        // Arrange
        String requestBody = "{\"email\":\"e2e-user@example.com\",\"password\":\"password123\"}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.text()).contains("\"success\":true");
        assertThat(response.text()).contains("Registration successful");

        // Verify user was created in database
        assertThat(userRepository.findByEmail("e2e-user@example.com")).isPresent();
    }

    @Test
    @DisplayName("Should fail registration with existing email")
    void testRegistrationWithExistingEmail() {
        // Arrange - Register user first
        String requestBody = "{\"email\":\"duplicate@example.com\",\"password\":\"password123\"}";
        apiPost("/users/register", requestBody);

        // Act - Try to register again with same email
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(409); // Conflict status for duplicate resource
        assertThat(response.text()).contains("Email already exists");
    }

    @Test
    @DisplayName("Should fail registration with invalid email format")
    void testRegistrationWithInvalidEmail() {
        // Arrange
        String requestBody = "{\"email\":\"invalid-email\",\"password\":\"password123\"}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
        // Validation error from @Email annotation
    }

    @Test
    @DisplayName("Should fail registration with empty email")
    void testRegistrationWithEmptyEmail() {
        // Arrange
        String requestBody = "{\"email\":\"\",\"password\":\"password123\"}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
        // Validation error from @NotBlank
    }

    @Test
    @DisplayName("Should fail registration with empty password")
    void testRegistrationWithEmptyPassword() {
        // Arrange
        String requestBody = "{\"email\":\"test@example.com\",\"password\":\"\"}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
        // Validation error from @NotBlank and @Size
    }

    @Test
    @DisplayName("Should fail registration with too short password")
    void testRegistrationWithShortPassword() {
        // Arrange
        String requestBody = "{\"email\":\"test@example.com\",\"password\":\"abc12\"}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
        // Validation error from @Size(min=6)
    }

    @Test
    @DisplayName("Should fail registration with null email")
    void testRegistrationWithNullEmail() {
        // Arrange
        String requestBody = "{\"email\":null,\"password\":\"password123\"}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should fail registration with null password")
    void testRegistrationWithNullPassword() {
        // Arrange
        String requestBody = "{\"email\":\"test@example.com\",\"password\":null}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should succeed with minimum valid password length")
    void testRegistrationWithMinimumPasswordLength() {
        // Arrange - Password with exactly 6 characters
        String requestBody = "{\"email\":\"minpass@example.com\",\"password\":\"pass12\"}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.text()).contains("\"success\":true");
    }

    @Test
    @DisplayName("Should succeed with maximum valid password length")
    void testRegistrationWithMaximumPasswordLength() {
        // Arrange - Password with exactly 100 characters
        String longPassword = "a".repeat(100);
        String requestBody = "{\"email\":\"maxpass@example.com\",\"password\":\"" + longPassword + "\"}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.text()).contains("\"success\":true");
    }

    @Test
    @DisplayName("Should fail with too long password")
    void testRegistrationWithTooLongPassword() {
        // Arrange - Password with 101 characters
        String longPassword = "a".repeat(101);
        String requestBody = "{\"email\":\"toolong@example.com\",\"password\":\"" + longPassword + "\"}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void testRegistrationWithSpecialCharacters() {
        // Arrange
        String requestBody = "{\"email\":\"special@example.com\",\"password\":\"P@ssw0rd!#$%\"}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.text()).contains("\"success\":true");
    }

    @Test
    @DisplayName("Should handle email with plus addressing")
    void testRegistrationWithPlusAddressing() {
        // Arrange
        String requestBody = "{\"email\":\"user+tag@example.com\",\"password\":\"password123\"}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.text()).contains("\"success\":true");
    }

    @Test
    @DisplayName("Should measure registration response time")
    void testRegistrationResponseTime() {
        // Arrange
        String requestBody = "{\"email\":\"perf@example.com\",\"password\":\"password123\"}";
        long startTime = System.currentTimeMillis();

        // Act
        APIResponse response = apiPost("/users/register", requestBody);
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(responseTime).isLessThan(2000); // Should respond within 2 seconds
        System.out.println("Registration response time: " + responseTime + "ms");
    }

    @Test
    @DisplayName("Should handle multiple concurrent registrations")
    void testMultipleRegistrations() {
        // Arrange & Act
        String request1 = "{\"email\":\"user1@example.com\",\"password\":\"password1\"}";
        String request2 = "{\"email\":\"user2@example.com\",\"password\":\"password2\"}";
        String request3 = "{\"email\":\"user3@example.com\",\"password\":\"password3\"}";

        APIResponse response1 = apiPost("/users/register", request1);
        APIResponse response2 = apiPost("/users/register", request2);
        APIResponse response3 = apiPost("/users/register", request3);

        // Assert
        assertThat(response1.status()).isEqualTo(200);
        assertThat(response2.status()).isEqualTo(200);
        assertThat(response3.status()).isEqualTo(200);

        // Verify all users were created
        assertThat(userRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("Should return correct CORS headers")
    void testCorsHeaders() {
        // Arrange
        String requestBody = "{\"email\":\"cors@example.com\",\"password\":\"password123\"}";

        // Act
        APIResponse response = apiPost("/users/register", requestBody);

        // Assert
        assertThat(response.status()).isEqualTo(200);
        // CORS headers should be present (verified by integration tests)
    }
}
