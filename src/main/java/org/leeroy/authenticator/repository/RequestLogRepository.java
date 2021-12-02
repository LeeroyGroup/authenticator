package org.leeroy.authenticator.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import org.leeroy.authenticator.model.RequestLog;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RequestLogRepository implements ReactivePanacheMongoRepository<RequestLog> {
}
