package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.entity.UserEntity;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@example.com", "password123");

        testUser = new UserEntity("test@example.com", "password123");
        testUser.setId(1L);
    }

    // ==================== LOGIN ENDPOINT TESTS ====================

    @Test
    @DisplayName("POST /api/users/login - Success")
    void login_Success_Returns200() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse(true, "Login successful"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"));
    }

    @Test
    @DisplayName("POST /api/users/login - Fail returns 400")
    void login_Fail_Returns400() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenReturn(new LoginResponse(false, "User not found"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ==================== REGISTER ENDPOINT TESTS ====================

    @Test
    @DisplayName("POST /api/users/register - Success")
    void register_Success_Returns200() throws Exception {
        when(userService.register(any(LoginRequest.class)))
                .thenReturn(new LoginResponse(true, "Registration successful"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful"));
    }

    @Test
    @DisplayName("POST /api/users/register - Fail returns 400")
    void register_Fail_Returns400() throws Exception {
        when(userService.register(any(LoginRequest.class)))
                .thenReturn(new LoginResponse(false, "Email already exists"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }

    // ==================== GET USER ENDPOINT TESTS ====================

    @Test
    @DisplayName("GET /api/users/{id} - Success")
    void getUser_Success_Returns200() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUser);

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Not found returns 404")
    void getUser_NotFound_Returns404() throws Exception {
        when(userService.getUserById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== UPDATE ENDPOINT TESTS ====================

    @Test
    @DisplayName("PUT /api/users/{id} - Success")
    void updateUser_Success_Returns200() throws Exception {
        when(userService.updateUser(eq(1L), any(LoginRequest.class)))
                .thenReturn(new LoginResponse(true, "User updated successfully"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully"));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Fail returns 400")
    void updateUser_Fail_Returns400() throws Exception {
        when(userService.updateUser(eq(999L), any(LoginRequest.class)))
                .thenReturn(new LoginResponse(false, "User not found"));

        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ==================== DELETE ENDPOINT TESTS ====================

    @Test
    @DisplayName("DELETE /api/users/{id} - Success")
    void deleteUser_Success_Returns200() throws Exception {
        when(userService.deleteUser(1L))
                .thenReturn(new LoginResponse(true, "User deleted successfully"));

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Fail returns 400")
    void deleteUser_Fail_Returns400() throws Exception {
        when(userService.deleteUser(999L))
                .thenReturn(new LoginResponse(false, "User not found"));

        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    // ==================== VALIDATION ERROR TESTS ====================

    @Test
    @DisplayName("POST /api/users/register - Validation error with invalid email format")
    void testRegisterWithInvalidEmailFormat() throws Exception {
        LoginRequest request = new LoginRequest("invalid-email", "password123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/register - Validation error with too short password")
    void testRegisterWithTooShortPassword() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "abc12");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/register - Validation error with null email")
    void testRegisterWithNullEmail() throws Exception {
        LoginRequest request = new LoginRequest(null, "password123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/register - Validation error with null password")
    void testRegisterWithNullPassword() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", null);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/register - Validation error with blank email")
    void testRegisterWithBlankEmail() throws Exception {
        LoginRequest request = new LoginRequest("", "password123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/register - Validation error with blank password")
    void testRegisterWithBlankPassword() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/login - Validation error with invalid email format")
    void testLoginWithInvalidEmailFormat() throws Exception {
        LoginRequest request = new LoginRequest("not-an-email", "password123");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/login - Validation error with too short password")
    void testLoginWithTooShortPassword() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "abc");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Validation error with invalid data")
    void testUpdateWithInvalidData() throws Exception {
        LoginRequest request = new LoginRequest("invalid-email", "abc");

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/register - Validation error with whitespace-only email")
    void testRegisterWithWhitespaceOnlyEmail() throws Exception {
        LoginRequest request = new LoginRequest("   ", "password123");

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/register - Validation error with too long password")
    void testRegisterWithTooLongPassword() throws Exception {
        String longPassword = "a".repeat(101);
        LoginRequest request = new LoginRequest("test@example.com", longPassword);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users/register - Success with minimum valid password length")
    void testRegisterWithMinimumPasswordLength() throws Exception {
        LoginRequest request = new LoginRequest("test@example.com", "pass12");
        when(userService.register(any(LoginRequest.class)))
                .thenReturn(new LoginResponse(true, "User registered successfully"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/users/register - Success with valid email formats")
    void testRegisterWithVariousValidEmails() throws Exception {
        String[] validEmails = {
            "user@example.com",
            "user.name@example.com",
            "user+tag@example.co.uk"
        };

        for (String email : validEmails) {
            LoginRequest request = new LoginRequest(email, "password123");
            when(userService.register(any(LoginRequest.class)))
                    .thenReturn(new LoginResponse(true, "User registered successfully"));

            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}