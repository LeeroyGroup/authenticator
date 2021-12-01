package org.leeroy.authenticator.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.BlockedAccess;
import org.leeroy.authenticator.resource.RequestID;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class BlockedAccessRepository implements ReactivePanacheMongoRepository<BlockedAccess> {

    public Uni<BlockedAccess> findValid(RequestID requestID, int expirationInMinutes) {
        return find("ipAddress = ?1 and device = ?2 and timestamp > ?3", requestID.ipAddress, requestID.device, Instant.now().minus(expirationInMinutes, ChronoUnit.MINUTES))
                .firstResult();
    }
}
