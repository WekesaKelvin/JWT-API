package com.springjwt.services.Messages;


import com.springjwt.entities.MessageLog;
import com.springjwt.repositories.MessageLogRepository;
import com.springjwt.services.Email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ProductMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ProductMessageListener.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private MessageLogRepository messageLogRepository;

    @JmsListener(destination = "product-queue")
    public void onMessage(String message) {
        try {
            logger.info("Received message from product.queue: {}", message);
            // Save to database
            MessageLog log = new MessageLog(message, new Date());
            messageLogRepository.save(log);
            logger.info("Message saved to database with ID: {}", log.getId());

            // Parse message to extract product name and user email
            String[] parts = message.split(" by ");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid message format: " + message);
            }
            String productName = parts[0].replace("Created product: ", "").trim();
            String userEmail = parts[1].trim();

            // Send email to user
            String subject = "Product Creation Confirmation";
            String body = String.format("Dear User,\n\nYour product '%s' has been successfully created.\n\nRegards,\nThe Team", productName);
            emailService.sendEmail(userEmail, subject, body);
            logger.info("Email sent to {} for product '{}'", userEmail, productName);

        } catch (Exception e) {
            logger.error("Failed to process message '{}': {}", message, e.getMessage(), e);
            // Rethrow to trigger JMS redelivery or move to a dead-letter queue
            throw new RuntimeException("Message processing failed: " + message, e);
        }
    }
}
