package com.springjwt.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class ProductMessageConsumer {

    private final Logger logger = LoggerFactory.getLogger(ProductMessageConsumer.class);

    @JmsListener(destination = "${consumer.queue.name:product-queue}")
    public void receiveMessage(String message) {
        logger.info("Received message from product-queue: {}", message);
    }

}
