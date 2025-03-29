package com.springjwt.Controller;

import com.springjwt.controllers.AuthenticationController;
import com.springjwt.dto.AuthenticationDTO;
import com.springjwt.dto.AuthenticationResponse;
import com.springjwt.services.jwt.UserDetailsServiceImpl;
import com.springjwt.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthenticationControllerTest {

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    public void testCreateAuthenticationToken_Success() throws Exception {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setEmail("test@example.com");
        authDTO.setPassword("password123");

        UserDetails userDetails = new User("test@example.com", "encodedPassword", true, true, true, true, Arrays.asList());
        String jwt = "jwt.token.example";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // Authentication success
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken("test@example.com")).thenReturn(jwt);

        // Act
        AuthenticationResponse result = authenticationController.createAuthenticationToken(authDTO, response);

        // Assert
        assertNotNull(result);
        assertEquals(jwt, result.jwtToken()); // Adjusted to match your likely getter
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername("test@example.com");
        verify(jwtUtil, times(1)).generateToken("test@example.com");
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    public void testCreateAuthenticationToken_BadCredentials() throws IOException {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setEmail("test@example.com");
        authDTO.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Incorrect username or password!"));

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authenticationController.createAuthenticationToken(authDTO, response);
        });
        assertEquals("Incorrect username or password!", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(anyString());
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    public void testCreateAuthenticationToken_DisabledUser() throws IOException {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setEmail("disabled@example.com");
        authDTO.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("User is not activated"));
        doNothing().when(response).sendError(HttpServletResponse.SC_NOT_FOUND, "User is not activated");

        // Act
        AuthenticationResponse result = authenticationController.createAuthenticationToken(authDTO, response);

        // Assert
        assertNull(result); // Method returns null after sending error
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(response, times(1)).sendError(HttpServletResponse.SC_NOT_FOUND, "User is not activated");
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    public void testCreateAuthenticationToken_UsernameNotFound() throws IOException {
        // Arrange
        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setEmail("unknown@example.com");
        authDTO.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null); // Authentication succeeds
        when(userDetailsService.loadUserByUsername("unknown@example.com"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class, () -> {
            authenticationController.createAuthenticationToken(authDTO, response);
        });
        assertEquals("User not found", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userDetailsService, times(1)).loadUserByUsername("unknown@example.com");
        verify(jwtUtil, never()).generateToken(anyString());
        verify(response, never()).sendError(anyInt(), anyString());
    }
}