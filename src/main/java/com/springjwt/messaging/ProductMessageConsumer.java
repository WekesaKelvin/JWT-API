package com.springjwt.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class ProductMessageConsumer {

    private static Logger logger = LoggerFactory.getLogger(ProductMessageConsumer.class);

    @JmsListener(destination = "product-queue")
    public void receiveMessage(String message) {
        logger.info("Received message from product-queue: {}", message);
        // Add logic here, e.g., notify admins, log to a file
    }

}
