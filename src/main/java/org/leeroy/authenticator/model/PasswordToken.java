package org.leeroy.authenticator.model;

import org.bson.types.ObjectId;

import java.time.Instant;

public class PasswordToken {
    public ObjectId id;
    public String token;
    public String username;
    public Instant timestamp;
}
