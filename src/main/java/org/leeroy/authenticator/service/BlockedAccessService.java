package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.BlockedAccess;

public interface BlockedAccessService {

    Uni<Boolean> isBlocked(String ipAddress, String device);

    void blockIP(BlockedAccess blockedAccess);

}
