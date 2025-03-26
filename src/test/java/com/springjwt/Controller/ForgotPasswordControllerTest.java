package com.springjwt.Controller;

import com.springjwt.controllers.ForgotPassController;
import com.springjwt.services.auth.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ForgotPasswordControllerTest {

    @InjectMocks
    private ForgotPassController forgotPassController;

    @Mock
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    public void testForgotPassword_Success() {

        String email = "test@example.com";
        String token = "reset-token-123";
        when(authService.generatePasswordResetToken(email)).thenReturn(token);

        ResponseEntity<String> response = forgotPassController.forgotPassword(email);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset link sent to your email.", response.getBody());
        verify(authService, times(1)).generatePasswordResetToken(email);
    }

    @Test
    public void testForgotPassword_UserNotFound() {

        String email = "unknown@example.com";
        when(authService.generatePasswordResetToken(email)).thenReturn(null);

        ResponseEntity<String> response = forgotPassController.forgotPassword(email);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found with email: " + email, response.getBody());
        verify(authService, times(1)).generatePasswordResetToken(email);
    }

    @Test
    public void testResetPassword_Success() {

        String token = "valid-token-123";
        String newPassword = "newPass123";
        when(authService.resetPassword(token, newPassword)).thenReturn(true);

        ResponseEntity<String> response = forgotPassController.resetPassword(token, newPassword);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password reset successfully.", response.getBody());
        verify(authService, times(1)).resetPassword(token, newPassword);
    }

    @Test
    public void testResetPassword_InvalidOrExpiredToken() {

        String token = "invalid-token-123";
        String newPassword = "newPass123";
        when(authService.resetPassword(token, newPassword)).thenReturn(false);

        ResponseEntity<String> response = forgotPassController.resetPassword(token, newPassword);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or expired token.", response.getBody());
        verify(authService, times(1)).resetPassword(token, newPassword);
    }
}