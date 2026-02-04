package org.example.util;

import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.dto.UserUpdateRequest;
import org.example.entity.UserEntity;

import java.util.Random;
import java.util.UUID;

/**
 * Utility class for building test data objects.
 * Provides builder methods and predefined test data to reduce code duplication.
 */
public class TestDataBuilder {

    private static final Random random = new Random();

    // Predefined test users
    public static final String VALID_EMAIL = "valid@example.com";
    public static final String VALID_PASSWORD = "password123";
    public static final String INVALID_EMAIL = "invalid-email";
    public static final String SHORT_PASSWORD = "abc12";
    public static final String LONG_PASSWORD = "a".repeat(101);

    private TestDataBuilder() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a UserEntity with default valid values
     */
    public static UserEntity createValidUserEntity() {
        return new UserEntity(VALID_EMAIL, VALID_PASSWORD);
    }

    /**
     * Creates a UserEntity with specified email and password
     */
    public static UserEntity createUserEntity(String email, String password) {
        return new UserEntity(email, password);
    }

    /**
     * Creates a UserEntity with a random unique email
     */
    public static UserEntity createRandomUserEntity() {
        String randomEmail = "user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        return new UserEntity(randomEmail, "password" + random.nextInt(10000));
    }

    /**
     * Creates a UserEntity with ID (for mocking)
     */
    public static UserEntity createUserEntityWithId(Long id, String email, String password) {
        UserEntity user = new UserEntity(email, password);
        user.setId(id);
        return user;
    }

    /**
     * Creates a LoginRequest with default valid values
     */
    public static LoginRequest createValidLoginRequest() {
        return new LoginRequest(VALID_EMAIL, VALID_PASSWORD);
    }

    /**
     * Creates a LoginRequest with specified email and password
     */
    public static LoginRequest createLoginRequest(String email, String password) {
        return new LoginRequest(email, password);
    }

    /**
     * Creates a LoginRequest with invalid email
     */
    public static LoginRequest createInvalidEmailLoginRequest() {
        return new LoginRequest(INVALID_EMAIL, VALID_PASSWORD);
    }

    /**
     * Creates a LoginRequest with short password
     */
    public static LoginRequest createShortPasswordLoginRequest() {
        return new LoginRequest(VALID_EMAIL, SHORT_PASSWORD);
    }

    /**
     * Creates a LoginRequest with long password
     */
    public static LoginRequest createLongPasswordLoginRequest() {
        return new LoginRequest(VALID_EMAIL, LONG_PASSWORD);
    }

    /**
     * Creates a LoginRequest with null email
     */
    public static LoginRequest createNullEmailLoginRequest() {
        return new LoginRequest(null, VALID_PASSWORD);
    }

    /**
     * Creates a LoginRequest with null password
     */
    public static LoginRequest createNullPasswordLoginRequest() {
        return new LoginRequest(VALID_EMAIL, null);
    }

    /**
     * Creates a LoginRequest with blank email
     */
    public static LoginRequest createBlankEmailLoginRequest() {
        return new LoginRequest("", VALID_PASSWORD);
    }

    /**
     * Creates a LoginRequest with blank password
     */
    public static LoginRequest createBlankPasswordLoginRequest() {
        return new LoginRequest(VALID_EMAIL, "");
    }

    /**
     * Creates a LoginRequest with whitespace-only email
     */
    public static LoginRequest createWhitespaceEmailLoginRequest() {
        return new LoginRequest("   ", VALID_PASSWORD);
    }

    /**
     * Creates a LoginRequest with whitespace-only password
     */
    public static LoginRequest createWhitespacePasswordLoginRequest() {
        return new LoginRequest(VALID_EMAIL, "      ");
    }

    /**
     * Creates a LoginRequest with random unique email
     */
    public static LoginRequest createRandomLoginRequest() {
        String randomEmail = "user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        return new LoginRequest(randomEmail, "password" + random.nextInt(10000));
    }

    /**
     * Creates a UserUpdateRequest with specified email and password
     */
    public static UserUpdateRequest createUserUpdateRequest(String email, String password) {
        return new UserUpdateRequest(email, password, null);
    }

    /**
     * Creates a UserUpdateRequest with specified email, password, and role
     */
    public static UserUpdateRequest createUserUpdateRequest(String email, String password, String role) {
        return new UserUpdateRequest(email, password, role);
    }

    /**
     * Creates a UserUpdateRequest with default valid values
     */
    public static UserUpdateRequest createValidUserUpdateRequest() {
        return new UserUpdateRequest(VALID_EMAIL, VALID_PASSWORD, null);
    }

    /**
     * Creates a successful LoginResponse
     */
    public static LoginResponse createSuccessLoginResponse() {
        return new LoginResponse(true, "Operation successful");
    }

    /**
     * Creates a successful LoginResponse with custom message
     */
    public static LoginResponse createSuccessLoginResponse(String message) {
        return new LoginResponse(true, message);
    }

    /**
     * Creates a failure LoginResponse
     */
    public static LoginResponse createFailureLoginResponse() {
        return new LoginResponse(false, "Operation failed");
    }

    /**
     * Creates a failure LoginResponse with custom message
     */
    public static LoginResponse createFailureLoginResponse(String message) {
        return new LoginResponse(false, message);
    }

    /**
     * Creates a LoginResponse for user registration success
     */
    public static LoginResponse createRegistrationSuccessResponse() {
        return new LoginResponse(true, "User registered successfully");
    }

    /**
     * Creates a LoginResponse for email already exists error
     */
    public static LoginResponse createEmailExistsResponse() {
        return new LoginResponse(false, "Email already exists");
    }

    /**
     * Creates a LoginResponse for user not found error
     */
    public static LoginResponse createUserNotFoundResponse() {
        return new LoginResponse(false, "User not found");
    }

    /**
     * Creates a LoginResponse for invalid password error
     */
    public static LoginResponse createInvalidPasswordResponse() {
        return new LoginResponse(false, "Invalid password");
    }

    /**
     * Creates a LoginResponse for login success
     */
    public static LoginResponse createLoginSuccessResponse() {
        return new LoginResponse(true, "Login successful");
    }

    /**
     * Creates a LoginResponse for update success
     */
    public static LoginResponse createUpdateSuccessResponse() {
        return new LoginResponse(true, "User updated successfully");
    }

    /**
     * Creates a LoginResponse for delete success
     */
    public static LoginResponse createDeleteSuccessResponse() {
        return new LoginResponse(true, "User deleted successfully");
    }

    /**
     * Generates a random valid email
     */
    public static String generateRandomEmail() {
        return "user" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    /**
     * Generates a random password
     */
    public static String generateRandomPassword() {
        return "password" + random.nextInt(10000);
    }

    /**
     * Generates a long email (for testing length limits)
     */
    public static String generateLongEmail(int length) {
        String localPart = "a".repeat(Math.max(0, length - 13)); // 13 chars for @example.com
        return localPart + "@example.com";
    }

    /**
     * Generates a password of specific length
     */
    public static String generatePasswordOfLength(int length) {
        return "a".repeat(length);
    }

    /**
     * Creates an array of test LoginRequests with various valid inputs
     */
    public static LoginRequest[] createValidLoginRequests() {
        return new LoginRequest[]{
            new LoginRequest("user1@example.com", "password123"),
            new LoginRequest("user.name@example.com", "securePass456"),
            new LoginRequest("user+tag@example.co.uk", "P@ssw0rd!"),
            new LoginRequest("user_name@example.org", "testPass789")
        };
    }

    /**
     * Creates an array of test UserEntities
     */
    public static UserEntity[] createMultipleUsers(int count) {
        UserEntity[] users = new UserEntity[count];
        for (int i = 0; i < count; i++) {
            users[i] = new UserEntity(
                "user" + (i + 1) + "@example.com",
                "password" + (i + 1)
            );
        }
        return users;
    }

    /**
     * Builder class for UserEntity
     */
    public static class UserEntityBuilder {
        private Long id;
        private String email = VALID_EMAIL;
        private String password = VALID_PASSWORD;

        public UserEntityBuilder withId(Long id) {
            this.id = id;
            return this;
        }

        public UserEntityBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserEntityBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public UserEntityBuilder withRandomEmail() {
            this.email = generateRandomEmail();
            return this;
        }

        public UserEntity build() {
            UserEntity user = new UserEntity(email, password);
            if (id != null) {
                user.setId(id);
            }
            return user;
        }
    }

    /**
     * Builder class for LoginRequest
     */
    public static class LoginRequestBuilder {
        private String email = VALID_EMAIL;
        private String password = VALID_PASSWORD;

        public LoginRequestBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public LoginRequestBuilder withPassword(String password) {
            this.password = password;
            return this;
        }

        public LoginRequestBuilder withRandomEmail() {
            this.email = generateRandomEmail();
            return this;
        }

        public LoginRequestBuilder withInvalidEmail() {
            this.email = INVALID_EMAIL;
            return this;
        }

        public LoginRequestBuilder withNullEmail() {
            this.email = null;
            return this;
        }

        public LoginRequestBuilder withNullPassword() {
            this.password = null;
            return this;
        }

        public LoginRequest build() {
            return new LoginRequest(email, password);
        }
    }

    /**
     * Creates a UserEntity builder
     */
    public static UserEntityBuilder userEntity() {
        return new UserEntityBuilder();
    }

    /**
     * Creates a LoginRequest builder
     */
    public static LoginRequestBuilder loginRequest() {
        return new LoginRequestBuilder();
    }
}
