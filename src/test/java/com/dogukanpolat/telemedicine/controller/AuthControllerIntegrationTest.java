package com.dogukanpolat.telemedicine.controller;

import com.dogukanpolat.telemedicine.dto.auth.JwtResponseDto;
import com.dogukanpolat.telemedicine.dto.auth.UserLoginDto;
import com.dogukanpolat.telemedicine.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private UserLoginDto validLoginDto;
    private String testEmail;
    private String testPassword;
    private String testToken;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testPassword = "password123";
        testToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
        validLoginDto = new UserLoginDto(testEmail, testPassword);
    }

    @Test
    public void login_WithValidCredentials_ShouldReturnJwtResponse() throws Exception {
        when(authService.login(any(UserLoginDto.class)))
                .thenReturn(new JwtResponseDto(testToken));

        MvcResult result = mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(testToken))
                .andReturn();
    }

    @Test
    public void login_WithInvalidPassword_ShouldReturnUnauthorized() throws Exception {
        UserLoginDto invalidPasswordDto = new UserLoginDto(testEmail, "wrongpassword");
        when(authService.login(any(UserLoginDto.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPasswordDto)))
                // Expect 401 Unauthorized for bad credentials
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void login_WithNonExistingEmail_ShouldReturnUnauthorized() throws Exception {
        UserLoginDto nonExistingEmailDto = new UserLoginDto("nonexistent@example.com", testPassword);
        when(authService.login(any(UserLoginDto.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistingEmailDto)))
                // Expect 401 Unauthorized for unknown user
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void login_WithMissingEmail_ShouldReturnBadRequest() throws Exception {
        UserLoginDto missingEmailDto = new UserLoginDto(null, testPassword); // Email is null

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(missingEmailDto)))
                // Expect 400 Bad Request due to validation failure
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        UserLoginDto emptyPasswordDto = new UserLoginDto(testEmail, ""); // Password is empty

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyPasswordDto)))
                // Expect 400 Bad Request due to validation failure
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_WithInvalidEmailFormat_ShouldReturnBadRequest() throws Exception {
        UserLoginDto invalidEmailDto = new UserLoginDto("not-an-email", testPassword);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailDto)))
                // Expect 400 Bad Request due to validation failure
                .andExpect(status().isBadRequest());
    }

    @Test
    public void login_WithEmptyBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")) // Empty content
                // Expect 400 Bad Request (typically due to failed deserialization)
                .andExpect(status().isBadRequest());
    }
}
