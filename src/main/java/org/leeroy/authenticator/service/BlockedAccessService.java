package org.leeroy.authenticator.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.BlockedAccess;
import org.leeroy.authenticator.repository.BlockedAccessRepository;
import org.leeroy.authenticator.resource.ClientID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;

@ApplicationScoped
public class BlockedAccessService {

    @Inject
    BlockedAccessRepository blockedAccessRepository;

    public Uni<Boolean> isBlocked(ClientID clientID) {
        Log.info("isBlocked");

        return blockedAccessRepository.find("ipAddress = ?1 and device = ?2 and timestamp >= ?3",
                        clientID.ipAddress,
                        clientID.device,
                        LocalDateTime.now().minusMinutes(15))
                .count()
                .map(i -> i > 0);
    }

    public Uni<Void> block(ClientID clientID, String reason) {
        BlockedAccess blockedAccess = new BlockedAccess();
        blockedAccess.ipAddress = clientID.ipAddress;
        blockedAccess.device = clientID.device;
        blockedAccess.reason = reason;
        blockedAccess.timestamp = Instant.now();
        blockedAccessRepository.persist(blockedAccess);
        return Uni.createFrom().voidItem();
    }
}
