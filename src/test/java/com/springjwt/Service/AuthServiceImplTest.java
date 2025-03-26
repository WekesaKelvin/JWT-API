package com.springjwt.Service;

import com.springjwt.dto.SignupDTO;
import com.springjwt.dto.UserDTO;
import com.springjwt.entities.PasswordResetToken;
import com.springjwt.entities.User;
import com.springjwt.repositories.PasswordResetTokenRepository;
import com.springjwt.repositories.UserRepository;
import com.springjwt.services.Email.EmailService;
import com.springjwt.services.auth.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    // Test createUser method
    @Test
    public void testCreateUser_Success() {
        // Arrange
        SignupDTO signupDTO = new SignupDTO();
        signupDTO.setName("Bruce Guantai");
        signupDTO.setEmail("bruce.guantai@gmail.com");
        signupDTO.setPassword("password123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setName("Bruce Guantai");
        savedUser.setEmail("bruce.guantai@gmail.com");
        savedUser.setPassword("encodedPassword");

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = authService.createUser(signupDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Bruce Guantai", result.getName());
        assertEquals("bruce.guantai@gmail.com", result.getEmail());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    // Test generatePasswordResetToken method - Success case
    @Test
    public void testGeneratePasswordResetToken_Success() {

        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        when(userRepository.findFirstByEmail(email)).thenReturn(user);
        when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(new PasswordResetToken());

        String token = authService.generatePasswordResetToken(email);

        assertNotNull(token);
        assertEquals(36, token.length()); // UUID length
        verify(userRepository, times(1)).findFirstByEmail(email);
        verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
        verify(emailService, times(1)).sendResetPasswordEmail(email, token);
    }

    // Test generatePasswordResetToken method - User not found
    @Test
    public void testGeneratePasswordResetToken_UserNotFound() {

        String email = "unknown@example.com";
        when(userRepository.findFirstByEmail(email)).thenReturn(null);

        String token = authService.generatePasswordResetToken(email);

        assertNull(token);
        verify(userRepository, times(1)).findFirstByEmail(email);
        verify(tokenRepository, never()).save(any(PasswordResetToken.class));
        verify(emailService, never()).sendResetPasswordEmail(anyString(), anyString());
    }

    // Test resetPassword method - Success case
    @Test
    public void testResetPassword_Success() {

        String token = "valid-token";
        String newPassword = "newPass123";
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));

        when(tokenRepository.findByToken(token)).thenReturn(resetToken);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        boolean result = authService.resetPassword(token, newPassword);

        assertTrue(result);
        verify(tokenRepository, times(1)).findByToken(token);
        verify(passwordEncoder, times(1)).encode(newPassword);
        verify(userRepository, times(1)).save(user);
        verify(tokenRepository, times(1)).delete(resetToken);
    }

    // Test resetPassword method - Invalid token
    @Test
    public void testResetPassword_InvalidToken() {
        String token = "invalid-token";
        String newPassword = "newPass123";

        when(tokenRepository.findByToken(token)).thenReturn(null);

        boolean result = authService.resetPassword(token, newPassword);

        assertFalse(result);
        verify(tokenRepository, times(1)).findByToken(token);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(tokenRepository, never()).delete(any(PasswordResetToken.class));
    }

    // Test resetPassword method - Expired token
    @Test
    public void testResetPassword_ExpiredToken() {

        String token = "expired-token";
        String newPassword = "newPass123";
        User user = new User();
        user.setId(1L);

        PasswordResetToken resetToken = new PasswordResetToken(token, user);
        resetToken.setExpiryDate(LocalDateTime.now().minusHours(25));

        when(tokenRepository.findByToken(token)).thenReturn(resetToken);

        boolean result = authService.resetPassword(token, newPassword);

        assertFalse(result);
        verify(tokenRepository, times(1)).findByToken(token);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(tokenRepository, never()).delete(any(PasswordResetToken.class));
    }
}