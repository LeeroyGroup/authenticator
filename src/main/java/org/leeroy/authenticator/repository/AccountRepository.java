package org.leeroy.authenticator.repository;

import io.quarkus.mongodb.panache.PanacheMongoRepository;
import org.leeroy.authenticator.model.Account;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AccountRepository implements PanacheMongoRepository<Account> {
}
