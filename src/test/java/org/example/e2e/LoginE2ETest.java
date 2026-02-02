package org.example.e2e;

import com.microsoft.playwright.APIResponse;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User Login E2E Tests")
class LoginE2ETest extends BaseE2ETest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testSuccessfulLogin() {
        // Arrange - Register user first
        String registerBody = "{\"email\":\"login@example.com\",\"password\":\"password123\"}";
        apiPost("/users/register", registerBody);

        // Act - Login with same credentials
        String loginBody = "{\"email\":\"login@example.com\",\"password\":\"password123\"}";
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.text()).contains("\"success\":true");
        assertThat(response.text()).contains("Login successful");
    }

    @Test
    @DisplayName("Should fail login with non-existent email")
    void testLoginWithInvalidEmail() {
        // Arrange
        String loginBody = "{\"email\":\"nonexistent@example.com\",\"password\":\"password123\"}";

        // Act
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.text()).contains("\"success\":false");
        assertThat(response.text()).contains("User not found");
    }

    @Test
    @DisplayName("Should fail login with wrong password")
    void testLoginWithWrongPassword() {
        // Arrange - Register user
        String registerBody = "{\"email\":\"wrongpass@example.com\",\"password\":\"correctpass\"}";
        apiPost("/users/register", registerBody);

        // Act - Try to login with wrong password
        String loginBody = "{\"email\":\"wrongpass@example.com\",\"password\":\"wrongpass\"}";
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.text()).contains("\"success\":false");
        assertThat(response.text()).contains("Invalid password");
    }

    @Test
    @DisplayName("Should fail login with invalid email format")
    void testLoginWithInvalidEmailFormat() {
        // Arrange
        String loginBody = "{\"email\":\"invalid-email\",\"password\":\"password123\"}";

        // Act
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
        // Validation error from @Email
    }

    @Test
    @DisplayName("Should fail login with empty email")
    void testLoginWithEmptyEmail() {
        // Arrange
        String loginBody = "{\"email\":\"\",\"password\":\"password123\"}";

        // Act
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should fail login with empty password")
    void testLoginWithEmptyPassword() {
        // Arrange - Register user first
        String registerBody = "{\"email\":\"emptypass@example.com\",\"password\":\"password123\"}";
        apiPost("/users/register", registerBody);

        // Act
        String loginBody = "{\"email\":\"emptypass@example.com\",\"password\":\"\"}";
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should fail login with null email")
    void testLoginWithNullEmail() {
        // Arrange
        String loginBody = "{\"email\":null,\"password\":\"password123\"}";

        // Act
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should fail login with null password")
    void testLoginWithNullPassword() {
        // Arrange
        String loginBody = "{\"email\":\"test@example.com\",\"password\":null}";

        // Act
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should fail login with too short password")
    void testLoginWithShortPassword() {
        // Arrange
        String loginBody = "{\"email\":\"test@example.com\",\"password\":\"abc\"}";

        // Act
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should handle case-sensitive password correctly")
    void testLoginPasswordCaseSensitivity() {
        // Arrange - Register with specific password
        String registerBody = "{\"email\":\"case@example.com\",\"password\":\"Password123\"}";
        apiPost("/users/register", registerBody);

        // Act - Try login with different case
        String loginBody = "{\"email\":\"case@example.com\",\"password\":\"password123\"}";
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.text()).contains("Invalid password");
    }

    @Test
    @DisplayName("Should measure login response time")
    void testLoginResponseTime() {
        // Arrange - Register user
        String registerBody = "{\"email\":\"perf@example.com\",\"password\":\"password123\"}";
        apiPost("/users/register", registerBody);

        // Act
        String loginBody = "{\"email\":\"perf@example.com\",\"password\":\"password123\"}";
        long startTime = System.currentTimeMillis();
        APIResponse response = apiPost("/users/login", loginBody);
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(responseTime).isLessThan(2000); // Should respond within 2 seconds
        System.out.println("Login response time: " + responseTime + "ms");
    }

    @Test
    @DisplayName("Should handle login after password update")
    void testLoginAfterPasswordUpdate() {
        // Arrange - Register user
        String registerBody = "{\"email\":\"update@example.com\",\"password\":\"oldpass123\"}";
        APIResponse registerResponse = apiPost("/users/register", registerBody);

        // Get user ID from database
        Long userId = userRepository.findByEmail("update@example.com").get().getId();

        // Update password
        String updateBody = "{\"email\":\"update@example.com\",\"password\":\"newpass123\"}";
        apiPut("/users/" + userId, updateBody);

        // Act - Try login with new password
        String loginBody = "{\"email\":\"update@example.com\",\"password\":\"newpass123\"}";
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.text()).contains("\"success\":true");
    }

    @Test
    @DisplayName("Should handle multiple sequential login attempts")
    void testMultipleLoginAttempts() {
        // Arrange - Register user
        String registerBody = "{\"email\":\"multi@example.com\",\"password\":\"password123\"}";
        apiPost("/users/register", registerBody);

        String loginBody = "{\"email\":\"multi@example.com\",\"password\":\"password123\"}";

        // Act - Multiple login attempts
        APIResponse response1 = apiPost("/users/login", loginBody);
        APIResponse response2 = apiPost("/users/login", loginBody);
        APIResponse response3 = apiPost("/users/login", loginBody);

        // Assert - All should succeed
        assertThat(response1.status()).isEqualTo(200);
        assertThat(response2.status()).isEqualTo(200);
        assertThat(response3.status()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should handle special characters in credentials")
    void testLoginWithSpecialCharacters() {
        // Arrange - Register with special characters
        String registerBody = "{\"email\":\"special@example.com\",\"password\":\"P@ssw0rd!#$%\"}";
        apiPost("/users/register", registerBody);

        // Act
        String loginBody = "{\"email\":\"special@example.com\",\"password\":\"P@ssw0rd!#$%\"}";
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.text()).contains("\"success\":true");
    }

    @Test
    @DisplayName("Should fail login immediately after user deletion")
    void testLoginAfterDeletion() {
        // Arrange - Register user
        String registerBody = "{\"email\":\"delete@example.com\",\"password\":\"password123\"}";
        apiPost("/users/register", registerBody);

        // Get user ID from database
        Long userId = userRepository.findByEmail("delete@example.com").get().getId();

        // Delete user
        apiDelete("/users/" + userId);

        // Act - Try to login
        String loginBody = "{\"email\":\"delete@example.com\",\"password\":\"password123\"}";
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.text()).contains("User not found");
    }

    @Test
    @DisplayName("Should handle whitespace in credentials")
    void testLoginWithWhitespace() {
        // Arrange
        String loginBody = "{\"email\":\"   test@example.com   \",\"password\":\"password123\"}";

        // Act
        APIResponse response = apiPost("/users/login", loginBody);

        // Assert
        // Should fail validation or user not found
        assertThat(response.status()).isIn(400, 404);
    }
}
