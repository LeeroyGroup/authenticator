package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import org.jboss.logging.Logger;
import org.leeroy.authenticator.model.Attempt;
import org.leeroy.authenticator.repository.AttemptRepository;
import org.leeroy.authenticator.resource.RequestID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class AttemptService {

    @Inject
    AttemptRepository attemptRepository;

    private static final Logger LOG = Logger.getLogger(AttemptService.class);

    public Uni<Long> getAttempts(RequestID requestID, String attemptType, boolean successful, int minutesSpan) {
        return attemptRepository.find("ipAddress = ?1 and device = ?2 and attemptType = ?3 and successful = ?4 and timestamp >= ?5",
                        requestID.ipAddress,
                        requestID.device,
                        attemptType,
                        successful,
                        Instant.now().minus(minutesSpan, ChronoUnit.MINUTES))
                .count();
    }

    public Uni<Void> createAttempt(RequestID requestID, String username, String attemptType, boolean successful) {
        Attempt attempt = new Attempt();
        attempt.ipAddress = requestID.ipAddress;
        attempt.device = requestID.device;
        attempt.channel = requestID.channel;
        attempt.client = requestID.client;
        attempt.username = username;
        attempt.attemptType = attemptType;
        attempt.successful = successful;
        attempt.timestamp = Instant.now();

        if (successful) {
            LOG.info(Json.encodePrettily(attempt));
        } else {
            LOG.error(Json.encodePrettily(attempt));
        }

        return attemptRepository.persist(attempt).replaceWithVoid();
    }
}