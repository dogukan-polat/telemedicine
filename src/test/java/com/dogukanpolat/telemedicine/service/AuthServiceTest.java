package com.dogukanpolat.telemedicine.service;

import com.dogukanpolat.telemedicine.dto.auth.JwtResponseDto;
import com.dogukanpolat.telemedicine.dto.auth.UserLoginDto;
import com.dogukanpolat.telemedicine.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
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
    void login_WithValidCredentials_ShouldReturnJwtResponse() {
        // Arrange
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtUtils.generateToken(testEmail)).thenReturn(testToken);

        // Act
        JwtResponseDto response = authService.login(validLoginDto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(testToken);

        verify(authenticationManager, times(1)).authenticate(
                argThat(auth -> auth instanceof UsernamePasswordAuthenticationToken &&
                        auth.getPrincipal().equals(testEmail) &&
                        auth.getCredentials().equals(testPassword))
        );
        verify(jwtUtils, times(1)).generateToken(testEmail);
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowBadCredentialsException() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(validLoginDto))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, never()).generateToken(anyString());
    }

    @Test
    void login_WithNullEmail_ShouldAuthenticateWithNull() {
        // Arrange
        UserLoginDto loginDto = new UserLoginDto(null, testPassword);
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtUtils.generateToken(null)).thenReturn(testToken);

        // Act
        JwtResponseDto response = authService.login(loginDto);

        // Assert
        assertThat(response).isNotNull();
        verify(authenticationManager, times(1)).authenticate(
                argThat(auth -> auth.getPrincipal() == null)
        );
        verify(jwtUtils, times(1)).generateToken(null);
    }

    @Test
    void login_WithEmptyPassword_ShouldAttemptAuthentication() {
        // Arrange
        UserLoginDto loginDto = new UserLoginDto(testEmail, "");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginDto))
                .isInstanceOf(BadCredentialsException.class);

        verify(authenticationManager, times(1)).authenticate(
                argThat(auth -> auth.getCredentials().equals(""))
        );
    }

    @Test
    void login_ShouldCreateCorrectAuthenticationToken() {
        // Arrange
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtUtils.generateToken(testEmail)).thenReturn(testToken);

        // Act
        authService.login(validLoginDto);

        // Assert
        verify(authenticationManager).authenticate(
                argThat(token -> {
                    UsernamePasswordAuthenticationToken authToken =
                            (UsernamePasswordAuthenticationToken) token;
                    return authToken.getPrincipal().equals(testEmail) &&
                            authToken.getCredentials().equals(testPassword);
                })
        );
    }

    @Test
    void login_WhenTokenGenerationSucceeds_ShouldReturnValidResponse() {
        // Arrange
        Authentication mockAuthentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtUtils.generateToken(testEmail)).thenReturn(testToken);

        // Act
        JwtResponseDto response = authService.login(validLoginDto);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.token()).isNotNull();
        assertThat(response.token().isEmpty()).isFalse();
        assertThat(response.token()).isEqualTo(testToken);
    }
}