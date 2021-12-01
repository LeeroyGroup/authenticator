package org.leeroy.authenticator.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.BlockedAccess;
import org.leeroy.authenticator.repository.BlockedAccessRepository;
import org.leeroy.authenticator.resource.ClientID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import java.time.Instant;
import java.time.LocalDateTime;

@ApplicationScoped
public class BlockedAccessService {

    @Inject
    BlockedAccessRepository blockedAccessRepository;

    private static final String BLOCKED_EXCEPTION_MESSAGE = "You have to wait a while before you try again";

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

    public Uni<Void> validateNotBlocked(ClientID clientID) {
        return isBlocked(clientID).onItem().invoke(isBlocked -> {
            if (isBlocked) {
                Log.error(BLOCKED_EXCEPTION_MESSAGE);
                throw new BadRequestException(BLOCKED_EXCEPTION_MESSAGE);
            }
        }).chain(() -> Uni.createFrom().voidItem());
    }
}
