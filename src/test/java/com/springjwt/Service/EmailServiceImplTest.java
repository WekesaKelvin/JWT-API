package com.springjwt.Service;

import com.springjwt.services.Email.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EmailServiceImplTest {

    @InjectMocks
    private EmailServiceImpl emailService;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    public void testSendResetPasswordEmail_Success() {
        String to = "test@example.com";
        String token = "abc123-token";
        String expectedResetUrl = "http://localhost:8080/reset-password?token=" + token;
        String expectedSubject = "Password Reset Request";
        String expectedText = "To reset your password, click the link below:\n" + expectedResetUrl + "\nThis link is valid for 24 hours.";

        emailService.sendResetPasswordEmail(to, token);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message.getTo()[0].equals(to) &&
                        message.getSubject().equals(expectedSubject) &&
                        message.getText().equals(expectedText)
        ));
    }

    @Test
    public void testSendResetPasswordEmail_VerifyMessageContent() {
        String to = "user@example.com";
        String token = "xyz789-token";

        emailService.sendResetPasswordEmail(to, token);

        verify(mailSender).send(argThat((SimpleMailMessage message) -> {
            String resetUrl = "http://localhost:8080/reset-password?token=" + token;
            return message.getTo()[0].equals(to) &&
                    message.getSubject().equals("Password Reset Request") &&
                    message.getText().equals("To reset your password, click the link below:\n" + resetUrl + "\nThis link is valid for 24 hours.");
        }));
    }

    @Test
    public void testSendResetPasswordEmail_NullToken() {
        String to = "test@example.com";
        String token = null;
        String expectedResetUrl = "http://localhost:8080/reset-password?token=null";
        String expectedText = "To reset your password, click the link below:\n" + expectedResetUrl + "\nThis link is valid for 24 hours.";

        emailService.sendResetPasswordEmail(to, token);

        verify(mailSender, times(1)).send(argThat((SimpleMailMessage message) ->
                message.getTo()[0].equals(to) &&
                        message.getSubject().equals("Password Reset Request") &&
                        message.getText().equals(expectedText)
        ));
    }

    @Test
    public void testSendResetPasswordEmail_EmptyToAddress() {
        String to = "";
        String token = "abc123-token";

        emailService.sendResetPasswordEmail(to, token);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
