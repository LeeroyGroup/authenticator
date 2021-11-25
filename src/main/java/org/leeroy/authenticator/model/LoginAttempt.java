package org.leeroy.authenticator.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class LoginAttempt extends PanacheMongoEntity {
    private String ipAddress;
    private String device;
    private String channel;
    private String client;
    private String username;
    private LocalDateTime timestamp;
}
