package com.springjwt.Messages;

import com.springjwt.entities.MessageLog;
import com.springjwt.repositories.MessageLogRepository;
import com.springjwt.services.Email.EmailService;
import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.activemq.broker-url=vm://embedded-broker?broker.persistent=false",
        "test.queue.name=test.product.queue"
})
public class ProductMessageListenerIntegrationTest {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private MessageLogRepository messageLogRepository;

    @MockBean
    private EmailService emailService;

    private Connection connection;

    @BeforeEach
    public void setup() {
        messageLogRepository.deleteAll();
        try {
            connection = connectionFactory.createConnection();
            connection.start();
        } catch (JMSException e) {
            fail("Failed to start JMS connection: " + e.getMessage(), e);
        }
    }

    @Test
    public void testOnMessage_Success() throws InterruptedException {
        String message = "Created product: Laptop by bruceydev@gmail.com";
        doNothing().when(emailService).sendEmail(anyString(), anyString(), anyString());

        jmsTemplate.convertAndSend("test.product.queue", message);

        Thread.sleep(2000);

        List<MessageLog> logs = messageLogRepository.findAll();
        assertEquals(1, logs.size(), "One message should be logged");
        MessageLog log = logs.get(0);
        assertEquals(message, log.getMessage());
        assertNotNull(log.getTimestamp());

        verify(emailService, times(1)).sendEmail("bruceydev@gmail.com", "Product Created: Laptop",
                "Dear user, the product Laptop has been created successfully.");
    }

    @Test
    public void testOnMessage_InvalidMessageFormat() throws InterruptedException {
        String invalidMessage = "Created product: Laptop";

        jmsTemplate.convertAndSend("test.product.queue", invalidMessage);

        Thread.sleep(5000);

        List<MessageLog> logs = messageLogRepository.findAll();
        assertEquals(4, logs.size(), "Message should be logged 4 times due to redelivery");

        for (MessageLog log : logs) {
            assertEquals(invalidMessage, log.getMessage());
            assertNotNull(log.getTimestamp());
        }

        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());

        try {
            Object dlqMessage = jmsTemplate.receiveAndConvert("DLQ.test.product.queue");
            assertNotNull(dlqMessage, "Message should be in DLQ after max redeliveries");
            assertEquals(invalidMessage, dlqMessage.toString());
        } catch (Exception e) {
            fail("Failed to receive message from DLQ: " + e.getMessage(), e);
        }
    }

    @Test
    public void testOnMessage_RetryAndMoveToDLQ() throws InterruptedException {
        // Arrange
        String message = "Created product: Laptop by invalid@nonexistent.com";
        doThrow(new RuntimeException("Failed to send email"))
                .when(emailService).sendEmail(anyString(), anyString(), anyString());

        jmsTemplate.convertAndSend("test.product.queue", message);

        Thread.sleep(6000);

        List<MessageLog> logs = messageLogRepository.findAll();
        assertEquals(4, logs.size(), "Message should be logged 4 times due to redelivery");

        for (MessageLog log : logs) {
            assertEquals(message, log.getMessage());
            assertNotNull(log.getTimestamp());
        }

        verify(emailService, times(4)).sendEmail("invalid@nonexistent.com", "Product Created: Laptop",
                "Dear user, the product Laptop has been created successfully.");

        try {
            Object dlqMessage = jmsTemplate.receiveAndConvert("DLQ.test.product.queue");
            assertNotNull(dlqMessage, "Message should be in DLQ after retries");
            assertEquals(message, dlqMessage.toString());
        } catch (Exception e) {
            fail("Failed to receive message from DLQ: " + e.getMessage(), e);
        }
    }

    @AfterEach
    public void tearDown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (JMSException e) {
                // Log the error but don't fail the test
                System.err.println("Failed to close JMS connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}