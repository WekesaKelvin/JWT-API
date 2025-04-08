package com.springjwt.configuration;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.IndividualDeadLetterStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import jakarta.jms.ConnectionFactory;
import org.springframework.util.ErrorHandler;

@Configuration
@EnableJms
public class JmsConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JmsConfiguration.class);

    @Value("${spring.activemq.broker-url:tcp://localhost:61616}")
    private String brokerUrl;


    @Bean
    public ActiveMQConnectionFactory connectionFactory() {
        logger.info("Configuring ActiveMQConnectionFactory with broker URL: {}", brokerUrl);
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(brokerUrl);
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setMaximumRedeliveries(3);
        redeliveryPolicy.setInitialRedeliveryDelay(1000);
        redeliveryPolicy.setRedeliveryDelay(1000);
        redeliveryPolicy.setUseExponentialBackOff(false);
        factory.setRedeliveryPolicy(redeliveryPolicy);
        return factory;
    }

    @Bean(name = "embedded-broker", initMethod = "start", destroyMethod = "stop")
    public BrokerService brokerService(@Value("${test.queue.name:product-queue}") String queueName) throws Exception {

        if (!brokerUrl.startsWith("vm://")) {
            logger.info("Broker URL {} does not start with 'vm://', skipping embedded broker creation", brokerUrl);
            return null;
        }

        logger.info("Starting embedded ActiveMQ broker for URL: {}", brokerUrl);
        BrokerService broker = new BrokerService();

        String brokerName = brokerUrl.split("\\?")[0].replace("vm://", "");
        broker.setBrokerName(brokerName);
        broker.setPersistent(false);
        broker.setUseJmx(false);

        PolicyMap policyMap = new PolicyMap();
        PolicyEntry policyEntry = new PolicyEntry();
        policyEntry.setQueue(queueName);

        IndividualDeadLetterStrategy deadLetterStrategy = new IndividualDeadLetterStrategy();
        deadLetterStrategy.setQueuePrefix("DLQ.");
        deadLetterStrategy.setUseQueueForQueueMessages(true);
        policyEntry.setDeadLetterStrategy(deadLetterStrategy);

        policyMap.setPolicyEntries(java.util.Arrays.asList(policyEntry));
        broker.setDestinationPolicy(policyMap);

        logger.info("Embedded ActiveMQ broker configured with name: {}", brokerName);
        return broker;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate();
        jmsTemplate.setConnectionFactory(connectionFactory);
        return jmsTemplate;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(ActiveMQConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrency("1-1");
        factory.setSessionTransacted(true);

        factory.setErrorHandler(new ErrorHandler() {
            @Override
            public void handleError(Throwable t) {
                logger.error("Error in JMS listener: {}", t.getMessage(), t);
            }
        });

        return factory;
    }

}