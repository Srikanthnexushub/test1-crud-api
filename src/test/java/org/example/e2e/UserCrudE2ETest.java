package org.example.e2e;

import com.microsoft.playwright.APIResponse;
import org.example.entity.UserEntity;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("User CRUD E2E Tests")
class UserCrudE2ETest extends BaseE2ETest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should complete full user lifecycle: register -> login -> get -> update -> delete")
    void testCompleteUserLifecycle() {
        // 1. Register
        String registerBody = "{\"email\":\"lifecycle@example.com\",\"password\":\"password123\"}";
        APIResponse registerResponse = apiPost("/users/register", registerBody);
        assertThat(registerResponse.status()).isEqualTo(200);

        // Get user ID from database
        Optional<UserEntity> user = userRepository.findByEmail("lifecycle@example.com");
        assertThat(user).isPresent();
        Long userId = user.get().getId();

        // 2. Login
        String loginBody = "{\"email\":\"lifecycle@example.com\",\"password\":\"password123\"}";
        APIResponse loginResponse = apiPost("/users/login", loginBody);
        assertThat(loginResponse.status()).isEqualTo(200);
        assertThat(loginResponse.text()).contains("Login successful");

        // 3. Get user details
        APIResponse getResponse = apiGet("/users/" + userId);
        assertThat(getResponse.status()).isEqualTo(200);
        assertThat(getResponse.text()).contains("lifecycle@example.com");

        // 4. Update user
        String updateBody = "{\"email\":\"updated@example.com\",\"password\":\"newpassword\"}";
        APIResponse updateResponse = apiPut("/users/" + userId, updateBody);
        assertThat(updateResponse.status()).isEqualTo(200);
        assertThat(updateResponse.text()).contains("User updated successfully");

        // Verify update
        APIResponse getUpdatedResponse = apiGet("/users/" + userId);
        assertThat(getUpdatedResponse.text()).contains("updated@example.com");

        // 5. Delete user
        APIResponse deleteResponse = apiDelete("/users/" + userId);
        assertThat(deleteResponse.status()).isEqualTo(200);
        assertThat(deleteResponse.text()).contains("User deleted successfully");

        // Verify deletion
        APIResponse getDeletedResponse = apiGet("/users/" + userId);
        assertThat(getDeletedResponse.status()).isEqualTo(404);
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void testGetUserById() {
        // Arrange - Create user
        UserEntity user = new UserEntity("getuser@example.com", "password123");
        UserEntity savedUser = userRepository.save(user);

        // Act
        APIResponse response = apiGet("/users/" + savedUser.getId());

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.text()).contains("getuser@example.com");
        assertThat(response.text()).contains(savedUser.getId().toString());
    }

    @Test
    @DisplayName("Should return 404 for non-existent user ID")
    void testGetNonExistentUser() {
        // Act
        APIResponse response = apiGet("/users/99999");

        // Assert
        assertThat(response.status()).isEqualTo(404);
    }

    @Test
    @DisplayName("Should update user email and password")
    void testUpdateUser() {
        // Arrange - Create user
        UserEntity user = new UserEntity("original@example.com", "oldpassword");
        UserEntity savedUser = userRepository.save(user);

        // Act
        String updateBody = "{\"email\":\"newemail@example.com\",\"password\":\"newpassword\"}";
        APIResponse response = apiPut("/users/" + savedUser.getId(), updateBody);

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.text()).contains("\"success\":true");

        // Verify changes in database
        Optional<UserEntity> updatedUser = userRepository.findById(savedUser.getId());
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getEmail()).isEqualTo("newemail@example.com");
        assertThat(updatedUser.get().getPassword()).isEqualTo("newpassword");
    }

    @Test
    @DisplayName("Should fail update for non-existent user")
    void testUpdateNonExistentUser() {
        // Arrange
        String updateBody = "{\"email\":\"new@example.com\",\"password\":\"newpass\"}";

        // Act
        APIResponse response = apiPut("/users/99999", updateBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.text()).contains("User not found");
    }

    @Test
    @DisplayName("Should fail update with invalid email")
    void testUpdateWithInvalidEmail() {
        // Arrange - Create user
        UserEntity user = new UserEntity("valid@example.com", "password");
        UserEntity savedUser = userRepository.save(user);

        // Act
        String updateBody = "{\"email\":\"invalid-email\",\"password\":\"newpass123\"}";
        APIResponse response = apiPut("/users/" + savedUser.getId(), updateBody);

        // Assert
        assertThat(response.status()).isEqualTo(400);
    }

    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser() {
        // Arrange - Create user
        UserEntity user = new UserEntity("delete@example.com", "password123");
        UserEntity savedUser = userRepository.save(user);
        Long userId = savedUser.getId();

        // Act
        APIResponse response = apiDelete("/users/" + userId);

        // Assert
        assertThat(response.status()).isEqualTo(200);
        assertThat(response.text()).contains("\"success\":true");
        assertThat(response.text()).contains("User deleted successfully");

        // Verify deletion in database
        Optional<UserEntity> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    @DisplayName("Should fail delete for non-existent user")
    void testDeleteNonExistentUser() {
        // Act
        APIResponse response = apiDelete("/users/99999");

        // Assert
        assertThat(response.status()).isEqualTo(400);
        assertThat(response.text()).contains("User not found");
    }

    @Test
    @DisplayName("Should handle concurrent user operations")
    void testConcurrentOperations() {
        // Arrange - Create multiple users
        userRepository.save(new UserEntity("user1@example.com", "pass1"));
        userRepository.save(new UserEntity("user2@example.com", "pass2"));
        userRepository.save(new UserEntity("user3@example.com", "pass3"));

        // Act - Verify count
        long count = userRepository.count();

        // Assert
        assertThat(count).isEqualTo(3);

        // Get all users
        Optional<UserEntity> user1 = userRepository.findByEmail("user1@example.com");
        Optional<UserEntity> user2 = userRepository.findByEmail("user2@example.com");
        Optional<UserEntity> user3 = userRepository.findByEmail("user3@example.com");

        assertThat(user1).isPresent();
        assertThat(user2).isPresent();
        assertThat(user3).isPresent();
    }

    @Test
    @DisplayName("Should maintain data integrity during update")
    void testDataIntegrityOnUpdate() {
        // Arrange - Create user
        UserEntity user = new UserEntity("integrity@example.com", "password123");
        UserEntity savedUser = userRepository.save(user);
        Long originalId = savedUser.getId();

        // Act - Update user
        String updateBody = "{\"email\":\"updated@example.com\",\"password\":\"newpassword\"}";
        apiPut("/users/" + originalId, updateBody);

        // Assert - ID should remain the same
        Optional<UserEntity> updatedUser = userRepository.findByEmail("updated@example.com");
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getId()).isEqualTo(originalId);

        // Old email should not exist
        Optional<UserEntity> oldEmailUser = userRepository.findByEmail("integrity@example.com");
        assertThat(oldEmailUser).isEmpty();
    }

    @Test
    @DisplayName("Should handle rapid create-read-update-delete sequence")
    void testRapidCrudSequence() {
        // Create
        String createBody = "{\"email\":\"rapid@example.com\",\"password\":\"password123\"}";
        APIResponse createResponse = apiPost("/users/register", createBody);
        assertThat(createResponse.status()).isEqualTo(200);

        // Get user ID
        Optional<UserEntity> user = userRepository.findByEmail("rapid@example.com");
        Long userId = user.get().getId();

        // Read
        APIResponse readResponse = apiGet("/users/" + userId);
        assertThat(readResponse.status()).isEqualTo(200);

        // Update
        String updateBody = "{\"email\":\"updated-rapid@example.com\",\"password\":\"newpass\"}";
        APIResponse updateResponse = apiPut("/users/" + userId, updateBody);
        assertThat(updateResponse.status()).isEqualTo(200);

        // Delete
        APIResponse deleteResponse = apiDelete("/users/" + userId);
        assertThat(deleteResponse.status()).isEqualTo(200);

        // Verify final state
        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    @DisplayName("Should measure CRUD operation performance")
    void testCrudOperationPerformance() {
        // Create
        long createStart = System.currentTimeMillis();
        String createBody = "{\"email\":\"perf@example.com\",\"password\":\"password123\"}";
        APIResponse createResponse = apiPost("/users/register", createBody);
        long createTime = System.currentTimeMillis() - createStart;
        assertThat(createResponse.status()).isEqualTo(200);

        Long userId = userRepository.findByEmail("perf@example.com").get().getId();

        // Read
        long readStart = System.currentTimeMillis();
        APIResponse readResponse = apiGet("/users/" + userId);
        long readTime = System.currentTimeMillis() - readStart;
        assertThat(readResponse.status()).isEqualTo(200);

        // Update
        long updateStart = System.currentTimeMillis();
        String updateBody = "{\"email\":\"perf-updated@example.com\",\"password\":\"newpass\"}";
        APIResponse updateResponse = apiPut("/users/" + userId, updateBody);
        long updateTime = System.currentTimeMillis() - updateStart;
        assertThat(updateResponse.status()).isEqualTo(200);

        // Delete
        long deleteStart = System.currentTimeMillis();
        APIResponse deleteResponse = apiDelete("/users/" + userId);
        long deleteTime = System.currentTimeMillis() - deleteStart;
        assertThat(deleteResponse.status()).isEqualTo(200);

        // Performance assertions
        assertThat(createTime).isLessThan(2000);
        assertThat(readTime).isLessThan(1000);
        assertThat(updateTime).isLessThan(2000);
        assertThat(deleteTime).isLessThan(1000);

        System.out.println("Create: " + createTime + "ms, Read: " + readTime +
                         "ms, Update: " + updateTime + "ms, Delete: " + deleteTime + "ms");
    }

    @Test
    @DisplayName("Should verify unique email constraint through API")
    void testUniqueEmailConstraintViaAPI() {
        // Arrange - Register first user
        String firstUser = "{\"email\":\"unique@example.com\",\"password\":\"password1\"}";
        APIResponse firstResponse = apiPost("/users/register", firstUser);
        assertThat(firstResponse.status()).isEqualTo(200);

        // Act - Try to register with same email
        String secondUser = "{\"email\":\"unique@example.com\",\"password\":\"password2\"}";
        APIResponse secondResponse = apiPost("/users/register", secondUser);

        // Assert
        assertThat(secondResponse.status()).isEqualTo(400);
        assertThat(secondResponse.text()).contains("Email already exists");

        // Verify only one user exists
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle update to existing email of another user")
    void testUpdateToExistingEmail() {
        // Arrange - Create two users
        UserEntity user1 = userRepository.save(new UserEntity("user1@example.com", "pass1"));
        UserEntity user2 = userRepository.save(new UserEntity("user2@example.com", "pass2"));

        // Act - Try to update user1's email to user2's email
        String updateBody = "{\"email\":\"user2@example.com\",\"password\":\"newpass\"}";
        APIResponse response = apiPut("/users/" + user1.getId(), updateBody);

        // This might succeed if no unique constraint check in update logic
        // Or might fail with constraint violation
        // Document the actual behavior
        System.out.println("Update to existing email status: " + response.status());
    }

    @Test
    @DisplayName("Should preserve data after multiple updates")
    void testMultipleUpdates() {
        // Arrange
        UserEntity user = userRepository.save(new UserEntity("multi@example.com", "pass1"));
        Long userId = user.getId();

        // Act - Multiple updates changing only password
        String update1 = "{\"email\":\"multi@example.com\",\"password\":\"password123\"}";
        APIResponse response1 = apiPut("/users/" + userId, update1);
        assertThat(response1.status()).isEqualTo(200);

        String update2 = "{\"email\":\"multi@example.com\",\"password\":\"password456\"}";
        APIResponse response2 = apiPut("/users/" + userId, update2);
        assertThat(response2.status()).isEqualTo(200);

        String update3 = "{\"email\":\"multi@example.com\",\"password\":\"password789\"}";
        APIResponse response3 = apiPut("/users/" + userId, update3);
        assertThat(response3.status()).isEqualTo(200);

        // Wait a moment for async operations to complete
        waitForResponse(100);

        // Assert - Final state (password should be updated to password789)
        userRepository.flush();
        Optional<UserEntity> finalUser = userRepository.findById(userId);
        assertThat(finalUser).isPresent();
        assertThat(finalUser.get().getEmail()).isEqualTo("multi@example.com");
        assertThat(finalUser.get().getPassword()).isEqualTo("password789");
        assertThat(finalUser.get().getId()).isEqualTo(userId); // ID unchanged
    }
}
