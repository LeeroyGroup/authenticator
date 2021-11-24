package org.leeroy.authenticator.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import org.leeroy.authenticator.model.BlockedAccess;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlockedIPRepository implements ReactivePanacheMongoRepository<BlockedAccess> {
}
