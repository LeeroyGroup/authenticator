package org.leeroy.authenticator.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.Account;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AccountRepository implements ReactivePanacheMongoRepository<Account> {
    public Uni<Account> findByUsername(String username) {
        return find("username", username).firstResult();
    }

    public Uni<Boolean> hasUser(String username) {
        return find("username", username).count().map(count -> count != 0);
    }
}
