package org.example.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoginResponse DTO Tests")
class LoginResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should create LoginResponse with default constructor")
    void testDefaultConstructor() {
        // Act
        LoginResponse response = new LoginResponse();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse(); // Default boolean value is false
        assertThat(response.getMessage()).isNull();
    }

    @Test
    @DisplayName("Should create LoginResponse with parameterized constructor")
    void testParameterizedConstructor() {
        // Act
        LoginResponse response = new LoginResponse(true, "Operation successful");

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Operation successful");
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testGettersAndSetters() {
        // Arrange
        LoginResponse response = new LoginResponse();

        // Act
        response.setSuccess(true);
        response.setMessage("Test message");

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Test message");
    }

    @Test
    @DisplayName("Should create successful response")
    void testSuccessResponse() {
        // Act
        LoginResponse response = new LoginResponse(true, "User logged in successfully");

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("User logged in successfully");
    }

    @Test
    @DisplayName("Should create failure response")
    void testFailureResponse() {
        // Act
        LoginResponse response = new LoginResponse(false, "Invalid credentials");

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    @DisplayName("Should serialize to JSON correctly")
    void testJsonSerialization() throws Exception {
        // Arrange
        LoginResponse response = new LoginResponse(true, "Serialization test");

        // Act
        String json = objectMapper.writeValueAsString(response);

        // Assert
        assertThat(json).isNotNull();
        assertThat(json).contains("\"success\":true");
        assertThat(json).contains("\"message\":\"Serialization test\"");
    }

    @Test
    @DisplayName("Should deserialize from JSON correctly")
    void testJsonDeserialization() throws Exception {
        // Arrange
        String json = "{\"success\":false,\"message\":\"Deserialization test\"}";

        // Act
        LoginResponse response = objectMapper.readValue(json, LoginResponse.class);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Deserialization test");
    }

    @Test
    @DisplayName("Should handle JSON with missing message field")
    void testJsonDeserializationWithMissingMessage() throws Exception {
        // Arrange
        String json = "{\"success\":true}";

        // Act
        LoginResponse response = objectMapper.readValue(json, LoginResponse.class);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isNull();
    }

    @Test
    @DisplayName("Should handle JSON with missing success field")
    void testJsonDeserializationWithMissingSuccess() throws Exception {
        // Arrange
        String json = "{\"message\":\"Test message\"}";

        // Act
        LoginResponse response = objectMapper.readValue(json, LoginResponse.class);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isFalse(); // Default boolean value
        assertThat(response.getMessage()).isEqualTo("Test message");
    }

    @Test
    @DisplayName("Should handle JSON with null message")
    void testJsonDeserializationWithNullMessage() throws Exception {
        // Arrange
        String json = "{\"success\":true,\"message\":null}";

        // Act
        LoginResponse response = objectMapper.readValue(json, LoginResponse.class);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isNull();
    }

    @Test
    @DisplayName("Should handle empty message")
    void testEmptyMessage() {
        // Arrange & Act
        LoginResponse response = new LoginResponse(true, "");

        // Assert
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEmpty();
    }

    @Test
    @DisplayName("Should handle null message")
    void testNullMessage() {
        // Arrange & Act
        LoginResponse response = new LoginResponse(false, null);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isNull();
    }

    @Test
    @DisplayName("Should handle long message")
    void testLongMessage() {
        // Arrange
        String longMessage = "This is a very long message that might be returned as an error or success message. "
            + "It contains multiple sentences and detailed information about the operation result. "
            + "The system should handle this without any issues.";

        // Act
        LoginResponse response = new LoginResponse(false, longMessage);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(longMessage);
    }

    @Test
    @DisplayName("Should handle message with special characters")
    void testMessageWithSpecialCharacters() {
        // Arrange
        String specialMessage = "Error: User not found! Please check @email & try again.";

        // Act
        LoginResponse response = new LoginResponse(false, specialMessage);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(specialMessage);
    }

    @Test
    @DisplayName("Should serialize and deserialize successfully")
    void testSerializationRoundTrip() throws Exception {
        // Arrange
        LoginResponse original = new LoginResponse(true, "Round trip test");

        // Act
        String json = objectMapper.writeValueAsString(original);
        LoginResponse deserialized = objectMapper.readValue(json, LoginResponse.class);

        // Assert
        assertThat(deserialized.isSuccess()).isEqualTo(original.isSuccess());
        assertThat(deserialized.getMessage()).isEqualTo(original.getMessage());
    }

    @Test
    @DisplayName("Should handle various success messages")
    void testVariousSuccessMessages() {
        // Test common success messages
        String[] successMessages = {
            "User registered successfully",
            "Login successful",
            "User updated successfully",
            "User deleted successfully"
        };

        for (String message : successMessages) {
            LoginResponse response = new LoginResponse(true, message);
            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo(message);
        }
    }

    @Test
    @DisplayName("Should handle various failure messages")
    void testVariousFailureMessages() {
        // Test common failure messages
        String[] failureMessages = {
            "User not found",
            "Invalid password",
            "Email already exists",
            "Invalid credentials"
        };

        for (String message : failureMessages) {
            LoginResponse response = new LoginResponse(false, message);
            assertThat(response.isSuccess()).isFalse();
            assertThat(response.getMessage()).isEqualTo(message);
        }
    }

    @Test
    @DisplayName("Should toggle success status")
    void testToggleSuccessStatus() {
        // Arrange
        LoginResponse response = new LoginResponse(true, "Initial message");

        // Act
        response.setSuccess(false);
        response.setMessage("Updated message");

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Updated message");
    }

    @Test
    @DisplayName("Should handle message with line breaks")
    void testMessageWithLineBreaks() {
        // Arrange
        String messageWithLineBreaks = "Error occurred:\nInvalid email format\nPlease try again";

        // Act
        LoginResponse response = new LoginResponse(false, messageWithLineBreaks);

        // Assert
        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo(messageWithLineBreaks);
        assertThat(response.getMessage()).contains("\n");
    }

    @Test
    @DisplayName("Should serialize boolean value correctly")
    void testBooleanSerialization() throws Exception {
        // Arrange
        LoginResponse trueResponse = new LoginResponse(true, "Test");
        LoginResponse falseResponse = new LoginResponse(false, "Test");

        // Act
        String trueJson = objectMapper.writeValueAsString(trueResponse);
        String falseJson = objectMapper.writeValueAsString(falseResponse);

        // Assert
        assertThat(trueJson).contains("\"success\":true");
        assertThat(falseJson).contains("\"success\":false");
    }
}
