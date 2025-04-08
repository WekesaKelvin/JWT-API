package com.springjwt.services.Messages;

import com.springjwt.entities.MessageLog;
import com.springjwt.repositories.MessageLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class MessageLogServiceImpl implements MessageLogService {

    private static final Logger logger = LoggerFactory.getLogger(MessageLogServiceImpl.class);

    @Autowired
    private MessageLogRepository messageLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveMessageLog(String message) {
        MessageLog log = new MessageLog();
        log.setMessage(message);
        LocalDateTime localDateTime = LocalDateTime.now();
        Date date = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        log.setTimestamp(date);
        MessageLog savedLog = messageLogRepository.save(log);
        logger.info("Logged message to database: {}", message);
    }

}
