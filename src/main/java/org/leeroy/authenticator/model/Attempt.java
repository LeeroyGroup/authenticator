package org.leeroy.authenticator.model;

import org.bson.types.ObjectId;

import java.time.Instant;

public class Attempt {
    public ObjectId id;
    public String ipAddress;
    public String device;
    public String channel;
    public String client;
    public String username;
    public Instant timestamp;
}
