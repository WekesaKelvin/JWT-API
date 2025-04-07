package com.springjwt.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "message_logs")
public class MessageLog {

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    @Column(nullable = false)
    private String message;

    @Setter
    @Getter
    @Column(nullable = false)
    private Date timestamp;

    public MessageLog() {}

    public MessageLog(String message, Date timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }
}
