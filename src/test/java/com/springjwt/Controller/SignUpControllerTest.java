package com.springjwt.Controller;

import com.springjwt.controllers.SignupController;
import com.springjwt.dto.SignupDTO;
import com.springjwt.dto.UserDTO;
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

public class SignUpControllerTest {

    @InjectMocks
    private SignupController signupController;

    @Mock
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    public void testSignupUser_Success() {
        // Arrange
        SignupDTO signupDTO = new SignupDTO();
        signupDTO.setName("Bruce Guantai");
        signupDTO.setEmail("bruce.guantai@gmail.com");
        signupDTO.setPassword("password123");

        UserDTO createdUser = new UserDTO();
        createdUser.setId(1L);
        createdUser.setName("Bruce Guantai");
        createdUser.setEmail("bruce.guantai@gmail.com");

        when(authService.createUser(signupDTO)).thenReturn(createdUser);

        ResponseEntity<?> response = signupController.signupUser(signupDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdUser, response.getBody());
        verify(authService, times(1)).createUser(signupDTO);
    }

    @Test
    public void testSignupUser_Failure() {

        SignupDTO signupDTO = new SignupDTO();
        signupDTO.setName("Bruce Guantai");
        signupDTO.setEmail("bruce.guantai@gmail.com");
        signupDTO.setPassword("password123");

        when(authService.createUser(signupDTO)).thenReturn(null);

        ResponseEntity<?> response = signupController.signupUser(signupDTO);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User not created, come again later!", response.getBody());
        verify(authService, times(1)).createUser(signupDTO);
    }
}