package org.leeroy.authenticator.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import org.leeroy.authenticator.model.BlockedIP;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class BlockedIPRepository implements PanacheMongoRepository<BlockedIP> {
}
