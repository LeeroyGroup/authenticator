package org.leeroy.authenticator.service;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.PasswordToken;
import org.leeroy.authenticator.repository.PasswordTokenRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

@ApplicationScoped
public class PasswordService {

    @Inject
    PasswordTokenRepository passwordTokenRepository;

    private static Pattern containsUpperCaseLetter = Pattern.compile("[A-Z]+");
    private static Pattern containsLowerCaseLetter = Pattern.compile("[a-z]+");
    private static Pattern containsDigit = Pattern.compile("[0-9]+");
    private static Pattern containsSpecialCharacter = Pattern.compile("[.#?!@$%^&*_-]+");

    private Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 32, 64);

    public Uni<String> hashPassword(String password) {
        String hash = argon2.hash(22, 65536, 1, password.toCharArray());
        argon2.wipeArray(password.toCharArray());
        return Uni.createFrom().item(hash);
    }

    public Uni<Boolean> validatePasswordStrength(String password) {
        if (password.length() < 8) {
            return Uni.createFrom().item(false);
        } else if (password.length() > 12) {
            return Uni.createFrom().item(true);
        }

        if (!containsUpperCaseLetter.matcher(password).find()) {
            return Uni.createFrom().item(false);
        }
        if (!containsLowerCaseLetter.matcher(password).find()) {
            return Uni.createFrom().item(false);
        }

        return Uni.createFrom().item(containsDigit.matcher(password).find() || containsSpecialCharacter.matcher(password).find());
    }

    public Uni<String> createSetPasswordToken(String username) {
        String token = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        PasswordToken passwordToken = new PasswordToken();
        passwordToken.token = token;
        passwordToken.username = username;
        passwordToken.timestamp = Instant.now();

        return passwordTokenRepository.persist(passwordToken)
                .map(item -> token);
    }

    public Uni<Void> isSetPasswordTokenValid(String token) {
        return passwordTokenRepository.getByToken(token).call(passwordToken -> {
            Long minutesPassed = Duration.between(passwordToken.timestamp, Instant.now()).toMinutes();
            if (minutesPassed > 10) {
                throw new BadRequestException();
            }
            return Uni.createFrom().voidItem();
        }).chain(() -> Uni.createFrom().voidItem());
    }
}