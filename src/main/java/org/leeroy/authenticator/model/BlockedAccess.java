package org.leeroy.authenticator.model;

import java.time.Instant;

public class BlockedAccess {
    public String ipAddress;
    public String device;
    public Instant timestamp;
    public String reason;
}
