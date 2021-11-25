package org.leeroy.authenticator.repository;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LoginAttemptRepository implements ReactivePanacheMongoRepository<PanacheMongoEntity> {
}
