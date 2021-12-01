package org.leeroy.authenticator.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.BlockedAccess;
import org.leeroy.authenticator.repository.BlockedAccessRepository;
import org.leeroy.authenticator.resource.RequestID;

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

    public Uni<Void> block(RequestID requestID, String reason) {
        BlockedAccess blockedAccess = new BlockedAccess();
        blockedAccess.ipAddress = requestID.ipAddress;
        blockedAccess.device = requestID.device;
        blockedAccess.reason = reason;
        blockedAccess.timestamp = Instant.now();

        return blockedAccessRepository.persist(blockedAccess).replaceWithVoid();
    }

    public Uni<Void> validateNotBlocked(RequestID requestID) {
        return isBlocked(requestID).invoke(isBlocked -> {
            if (isBlocked) {
                Log.error(BLOCKED_EXCEPTION_MESSAGE);
                throw new BadRequestException(BLOCKED_EXCEPTION_MESSAGE);
            }
        }).replaceWithVoid();
    }

    private Uni<Boolean> isBlocked(RequestID requestID) {
        return blockedAccessRepository.findValid(requestID, EXPIRED_AFTER_MINUTES).map(blockedAccess -> blockedAccess != null);
    }
}
