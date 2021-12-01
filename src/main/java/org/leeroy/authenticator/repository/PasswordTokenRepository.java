package org.leeroy.authenticator.repository;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.PasswordToken;

import javax.enterprise.context.ApplicationScoped;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@ApplicationScoped
public class PasswordTokenRepository implements ReactivePanacheMongoRepository<PasswordToken> {
    public Uni<PasswordToken> getByToken(String token) {
        return find("token", token).firstResult();
    }

    public Uni<String> getUsernameByToken(String token) {
        return getByToken(token).map(passwordToken -> passwordToken.token);
    }

    public Uni<PasswordToken> getByUsername(String username) {
        return find("username", username).firstResult();
    }

    public Uni<PasswordToken> findValidByToken(String token, int expirationInMinutes) {
        return find("token = ?1 and timestamp > ?2", token, Instant.now().minus(expirationInMinutes, ChronoUnit.MINUTES))
                .firstResult();
    }

    public Uni<PasswordToken> findValidByUsername(String username, int expirationInMinutes) {
        return find("username = ?1 and timestamp > ?2", username, Instant.now().minus(expirationInMinutes, ChronoUnit.MINUTES))
                .firstResult();
    }
}
