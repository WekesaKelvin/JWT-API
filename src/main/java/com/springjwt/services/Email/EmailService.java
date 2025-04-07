package com.springjwt.services.Email;

public interface EmailService {
    void sendResetPasswordEmail(String to, String token);
    void sendEmail(String to, String subject, String text);
}
