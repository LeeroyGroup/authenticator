package org.leeroy.authenticator.service.impl;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
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
    public boolean isBlocked(String ipAddress, String device) {

        ReactivePanacheQuery<BlockedAccess> blockedAccess = blockedAccessRepository
                .find("ipAddress = ?1 and device = ?2 and timestamp >= ?3",
                        ipAddress,
                        device,
                        LocalDateTime.now().minusMinutes(15));

        Long numRecords = blockedAccess.count()
                .map(i -> i)
                .await().indefinitely();

        return numRecords > 0;
    }

    @Override
    public void blockIP(BlockedAccess blockedAccess) {
        blockedAccessRepository.persist(blockedAccess);
    }
}
