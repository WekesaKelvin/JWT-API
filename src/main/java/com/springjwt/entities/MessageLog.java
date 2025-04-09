package com.springjwt.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "message_logs")
public class MessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String message;


    @Column(nullable = false)
    private Date timestamp;

    public MessageLog() {}

    public MessageLog(String message, Date timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }
}
