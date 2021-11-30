package org.leeroy.authenticator.service.impl;

import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.leeroy.authenticator.model.Attempt;
import org.leeroy.authenticator.repository.AttemptRepository;
import org.leeroy.authenticator.resource.ClientID;
import org.leeroy.authenticator.service.AttemptService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;

@ApplicationScoped
public class AttemptServiceImpl implements AttemptService {

    @Inject
    AttemptRepository attemptRepository;

    private static final Logger LOG = Logger.getLogger(AttemptServiceImpl.class);

    @Override
    public Uni<Long> getAttempts(ClientID clientID) {
        return attemptRepository.find("ipAddress = ?1 and device = ?2", clientID.ipAddress, clientID.device).count();
    }

    @Override
    public Uni<Void> createAttempt(ClientID clientID, String username, String attemptType, boolean valid) {
        Attempt attempt = new Attempt();
        attempt.ipAddress = clientID.ipAddress;
        attempt.device = clientID.device;
        attempt.channel = clientID.channel;
        attempt.client = clientID.name;
        attempt.username = username;
        attempt.timestamp = Instant.now();

        LOG.log(Logger.Level.INFO, attempt);
        attemptRepository.persist(attempt);

        return Uni.createFrom().voidItem();
    }
}
