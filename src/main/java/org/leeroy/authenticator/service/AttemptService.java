package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.leeroy.authenticator.model.Attempt;
import org.leeroy.authenticator.repository.AttemptRepository;
import org.leeroy.authenticator.resource.ClientID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class AttemptService {

    @Inject
    AttemptRepository attemptRepository;

    private static final Logger LOG = Logger.getLogger(AttemptService.class);

    public Uni<Long> getAttempts(ClientID clientID, int minutesSpan) {
        return attemptRepository.find("ipAddress = ?1 and device = ?2 and timestamp >= ?3", clientID.ipAddress, clientID.device, Instant.now().minus(minutesSpan, ChronoUnit.MINUTES))
                .count();
    }

    public Uni<Void> createAttempt(ClientID clientID, String username, String attemptType, boolean valid) {
        Attempt attempt = new Attempt();
        attempt.ipAddress = clientID.ipAddress;
        attempt.device = clientID.device;
        attempt.channel = clientID.channel;
        attempt.client = clientID.name;
        attempt.username = username;
        attempt.timestamp = Instant.now();

        LOG.log(Logger.Level.INFO, attempt);
        return attemptRepository.persist(attempt).replaceWithVoid();
    }
}