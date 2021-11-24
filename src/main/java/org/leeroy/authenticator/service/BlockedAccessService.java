package org.leeroy.authenticator.service;

import org.leeroy.authenticator.model.BlockedAccess;

public interface BlockedAccessService {

    boolean isBlocked(String ipAddress, String device);

    void blockIP(BlockedAccess blockedAccess);

}
