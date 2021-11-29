package org.leeroy.authenticator.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;

import java.time.Instant;

public class PasswordToken extends PanacheMongoEntity {
    public String token;
    public String username;
    public Instant timestamp;
}
