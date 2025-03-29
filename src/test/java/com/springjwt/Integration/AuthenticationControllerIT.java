package com.springjwt.Integration;

import com.springjwt.dto.AuthenticationDTO;
import com.springjwt.dto.AuthenticationResponse;
import com.springjwt.entities.User;
import com.springjwt.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthenticationControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);
    }

    @Test
    public void testAuthenticate_Success() {

        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setEmail("test@example.com");
        authDTO.setPassword("password123");

        HttpEntity<AuthenticationDTO> request = new HttpEntity<>(authDTO);

        ResponseEntity<AuthenticationResponse> response = restTemplate.exchange(
                "/authenticate",
                HttpMethod.POST,
                request,
                AuthenticationResponse.class
        );

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().jwtToken());
    }

    @Test
    public void testAuthenticate_BadCredentials() {

        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setEmail("test@example.com");
        authDTO.setPassword("wrongPassword");

        HttpEntity<AuthenticationDTO> request = new HttpEntity<>(authDTO);

        ResponseEntity<String> response = restTemplate.exchange(
                "/authenticate",
                HttpMethod.POST,
                request,
                String.class
        );

        assertEquals(401, response.getStatusCodeValue());
        String body = response.getBody();
        if (body != null) {
            assertTrue(body.contains("Incorrect username or password"));
        }
    }
}
