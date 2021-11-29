package org.leeroy.authenticator.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class LoginAttempt extends PanacheMongoEntity {
    public String ipAddress;
    public String device;
    public String channel;
    public String client;
    public String username;
    public Instant timestamp;
}
