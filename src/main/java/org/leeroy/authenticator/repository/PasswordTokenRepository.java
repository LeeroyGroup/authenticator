package org.leeroy.authenticator.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.PasswordToken;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordTokenRepository implements ReactivePanacheMongoRepository<PasswordToken> {
    public Uni<PasswordToken> getByToken(String token) {
        return find("token", token).firstResult();
    }

    public Uni<String> getUsernameByToken(String token) {
        return getByToken(token).map(passwordToken -> passwordToken.token);
    }
}
