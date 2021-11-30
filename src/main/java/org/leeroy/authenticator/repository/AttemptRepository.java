package org.leeroy.authenticator.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import org.leeroy.authenticator.model.Attempt;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AttemptRepository implements ReactivePanacheMongoRepository<Attempt> {
}
