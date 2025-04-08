package com.springjwt.services.Messages;

import com.springjwt.services.Email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class ProductMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(ProductMessageListener.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private MessageLogService messageLogService;

    @JmsListener(destination = "${test.queue.name:product-queue}")
    public void onMessage(String message) {

            logger.info("Received message from ${test.queue.name:product-queue}: {}", message);

            messageLogService.saveMessageLog(message);
        try {
            String[] parts = message.split(" by ");
            if (parts.length != 2) {
                logger.error("Invalid message format: {}", message);
                throw new IllegalArgumentException("Invalid message format: " + message);
            }

            String productPart = parts[0];
            String userEmail = parts[1];
            String productName = productPart.replace("Created product: ", "");

            String subject = "Product Created: " + productName;
            String body = "Dear user, the product " + productName + " has been created successfully.";
            emailService.sendEmail(userEmail, subject, body);

            logger.info("Processed message successfully: {}", message);
        } catch (IllegalArgumentException e) {
            logger.error("Failed to process message '{}': {}", message, e.getMessage());
            throw new RuntimeException("Message processing failed: " + message, e);
        } catch (Exception e) {
            logger.error("Unexpected error processing message '{}': {}", message, e.getMessage(), e);
            throw new RuntimeException("Message processing failed: " + message, e);
        }
    }


}
