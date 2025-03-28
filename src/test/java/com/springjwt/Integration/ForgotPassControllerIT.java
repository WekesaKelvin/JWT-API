package com.springjwt.Integration;

import com.springjwt.dto.AuthenticationDTO;
import com.springjwt.dto.AuthenticationResponse;
import com.springjwt.entities.User;
import com.springjwt.repositories.PasswordResetTokenRepository;
import com.springjwt.repositories.UserRepository;
import com.springjwt.services.Email.EmailService;
import com.springjwt.services.auth.AuthService;
import com.springjwt.services.auth.AuthServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.subethamail.smtp.MessageContext;
import org.subethamail.smtp.MessageHandler;
import org.subethamail.smtp.MessageHandlerFactory;
import org.subethamail.smtp.server.SMTPServer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ForgotPassControllerIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    private static SMTPServer smtpServer;
    private static JavaMailSender javaMailSender;

    // Test-specific EmailService implementation
    private static class TestEmailService implements EmailService {
        private final JavaMailSender mailSender;

        TestEmailService(JavaMailSender mailSender) {
            this.mailSender = mailSender;
        }

        @Override
        public void sendResetPasswordEmail(String to, String token) {
            String resetUrl = "http://localhost:8080/reset-password?token=" + token;
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Password Reset Request");
            message.setText("To reset your password, click the link below:\n" + resetUrl + "\nThis link is valid for 24 hours.");
            mailSender.send(message);
            System.out.println("Email sent to: " + to + " with token: " + token);
        }
    }

    // Corrected LoggingMessageHandlerFactory
    private static class LoggingMessageHandlerFactory implements MessageHandlerFactory {
        @Override
        public MessageHandler create(MessageContext ctx) {
            return new MessageHandler() {
                private String from;
                private String recipient;

                @Override
                public void from(String from) throws org.subethamail.smtp.RejectException {
                    this.from = from;
                }

                @Override
                public void recipient(String recipient) throws org.subethamail.smtp.RejectException {
                    this.recipient = recipient;
                }

                @Override
                public void data(InputStream data) throws java.io.IOException {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = data.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                    String emailContent = out.toString();
                    System.out.println("Received email from: " + from + " to: " + recipient + "\nContent:\n" + emailContent);
                }

                @Override
                public void done() {
                    // Cleanup if needed
                }
            };
        }
    }

    @BeforeAll
    public static void startSmtpServer() {
        // Start SubEthaSMTP server on port 2525 with corrected handler factory
        smtpServer = new SMTPServer(new LoggingMessageHandlerFactory());
        smtpServer.setPort(2525);
        smtpServer.start();

        // Configure JavaMailSender for SubEthaSMTP
        javaMailSender = new JavaMailSenderImpl();
        ((JavaMailSenderImpl) javaMailSender).setHost("localhost");
        ((JavaMailSenderImpl) javaMailSender).setPort(2525);
        ((JavaMailSenderImpl) javaMailSender).setProtocol("smtp");

        Properties props = ((JavaMailSenderImpl) javaMailSender).getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "false");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "true");
    }

    @AfterAll
    public static void stopSmtpServer() {
        if (smtpServer != null) {
            smtpServer.stop();
        }
    }

    @BeforeEach
    public void setup() {
        // Inject test EmailService into AuthService
        if (authService instanceof AuthServiceImpl) {
            ((AuthServiceImpl) authService).setEmailService(new TestEmailService(javaMailSender));
        } else {
            throw new IllegalStateException("AuthService is not an instance of AuthServiceImpl");
        }

        // Clear dependent tables first
        tokenRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);
    }

    @Test
    public void testForgotPassword_Success() {
        String email = "test@example.com";
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/forgot-password?email=" + email,
                null,
                String.class
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Password reset link sent to your email.", response.getBody());
    }

    @Test
    public void testForgotPassword_UserNotFound() {
        String email = "unknown@example.com";
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/forgot-password?email=" + email,
                null,
                String.class
        );

        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found with email: " + email, response.getBody());
    }

    @Test
    public void testResetPassword_Success() {
        String email = "test@example.com";
        String token = authService.generatePasswordResetToken(email);
        assertNotNull(token, "Reset token should be generated");

        String newPassword = "newPassword123";
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/reset-password?token=" + token + "&newPassword=" + newPassword,
                null,
                String.class
        );

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Password reset successfully.", response.getBody());

        AuthenticationDTO authDTO = new AuthenticationDTO();
        authDTO.setEmail("test@example.com");
        authDTO.setPassword("newPassword123");
        ResponseEntity<AuthenticationResponse> authResponse = restTemplate.postForEntity(
                "/authenticate",
                authDTO,
                AuthenticationResponse.class
        );
        assertEquals(200, authResponse.getStatusCodeValue());
    }

    @Test
    public void testResetPassword_InvalidToken() {
        String token = "invalid-token";
        String newPassword = "newPassword123";

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/reset-password?token=" + token + "&newPassword=" + newPassword,
                null,
                String.class
        );

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid or expired token.", response.getBody());
    }
}