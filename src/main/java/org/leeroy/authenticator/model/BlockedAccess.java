package org.leeroy.authenticator.model;

import org.bson.types.ObjectId;

import java.time.Instant;

public class BlockedAccess {
    public ObjectId id;
    public String ipAddress;
    public String device;
    public Instant timestamp;
    public String reason;
}
