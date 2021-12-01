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

@ApplicationScoped
public class BlockedAccessService {

    @Inject
    BlockedAccessRepository blockedAccessRepository;

    private static final String BLOCKED_EXCEPTION_MESSAGE = "You have to wait a while before you try again";

    private final static int EXPIRED_AFTER_MINUTES = 10;

    public Uni<Void> block(ClientID clientID, String reason) {
        BlockedAccess blockedAccess = new BlockedAccess();
        blockedAccess.ipAddress = clientID.ipAddress;
        blockedAccess.device = clientID.device;
        blockedAccess.reason = reason;
        blockedAccess.timestamp = Instant.now();

        return blockedAccessRepository.persist(blockedAccess).replaceWithVoid();
    }

    public Uni<Void> validateNotBlocked(ClientID clientID) {
        return isBlocked(clientID).invoke(isBlocked -> {
            if (isBlocked) {
                Log.error(BLOCKED_EXCEPTION_MESSAGE);
                throw new BadRequestException(BLOCKED_EXCEPTION_MESSAGE);
            }
        }).replaceWithVoid();
    }

    private Uni<Boolean> isBlocked(ClientID clientID) {
        return blockedAccessRepository.findValid(clientID, EXPIRED_AFTER_MINUTES).map(blockedAccess -> blockedAccess != null);
    }
}
