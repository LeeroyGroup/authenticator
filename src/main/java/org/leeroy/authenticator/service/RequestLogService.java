package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import org.jboss.logging.Logger;
import org.leeroy.authenticator.model.RequestLog;
import org.leeroy.authenticator.repository.RequestLogRepository;
import org.leeroy.authenticator.resource.RequestID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class RequestLogService {

    @Inject
    RequestLogRepository requestLogRepository;

    private static final Logger LOG = Logger.getLogger(RequestLogService.class);

    public Uni<Long> getRequests(RequestID requestID, String attemptType, boolean successful, int minutesSpan) {
        return requestLogRepository.find("ipAddress = ?1 and device = ?2 and attemptType = ?3 and successful = ?4 and timestamp >= ?5",
                        requestID.ipAddress,
                        requestID.device,
                        attemptType,
                        successful,
                        Instant.now().minus(minutesSpan, ChronoUnit.MINUTES))
                .count();
    }

    public Uni<Void> storeRequest(RequestID requestID, String username, String attemptType, boolean successful) {
        RequestLog requestLog = new RequestLog();
        requestLog.ipAddress = requestID.ipAddress;
        requestLog.device = requestID.device;
        requestLog.channel = requestID.channel;
        requestLog.client = requestID.client;
        requestLog.username = username;
        requestLog.attemptType = attemptType;
        requestLog.successful = successful;
        requestLog.timestamp = Instant.now();

        if (successful) {
            LOG.info(Json.encodePrettily(requestLog));
        } else {
            LOG.error(Json.encodePrettily(requestLog));
        }

        return requestLogRepository.persist(requestLog).replaceWithVoid();
    }
}