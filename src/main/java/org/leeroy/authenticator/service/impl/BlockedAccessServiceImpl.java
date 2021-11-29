package org.leeroy.authenticator.service.impl;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.BlockedAccess;
import org.leeroy.authenticator.repository.BlockedAccessRepository;
import org.leeroy.authenticator.service.BlockedAccessService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class BlockedAccessServiceImpl implements BlockedAccessService {

    @Inject
    BlockedAccessRepository blockedAccessRepository;

    @Override
    public Uni<Boolean> isBlocked(String ipAddress, String device) {
        Log.info("isBlocked");

        return blockedAccessRepository.find("ipAddress = ?1 and device = ?2 and timestamp >= ?3",
                        ipAddress,
                        device,
                        LocalDateTime.now().minusMinutes(15))
                .count()
                .map(i -> i > 0);
    }

    @Override
    public Uni<Void> blockIP(BlockedAccess blockedAccess) {
        blockedAccessRepository.persist(blockedAccess);
        return Uni.createFrom().voidItem();
    }
}
