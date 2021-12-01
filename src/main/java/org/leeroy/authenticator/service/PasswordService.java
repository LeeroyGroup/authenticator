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

    private static final String INVALID_PASSWORD_STRENGTH = "Invalid password strength";

    private final static Pattern containsUpperCaseLetter = Pattern.compile("[A-Z]+");
    private final static Pattern containsLowerCaseLetter = Pattern.compile("[a-z]+");
    private final static Pattern containsDigit = Pattern.compile("[0-9]+");
    private final static Pattern containsSpecialCharacter = Pattern.compile("[.#?!@$%^&*_-]+");

    private final static int EXPIRED_AFTER_MINUTES = 10;

    private final static Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id, 32, 64);

    public Uni<String> hashPassword(String password) {
        String hash = argon2.hash(22, 65536, 1, password.toCharArray());
        argon2.wipeArray(password.toCharArray());
        return Uni.createFrom().item(hash);
    }

    public Uni<Boolean> verifyPassword(String hash, String password) {
        return Uni.createFrom().item(argon2.verify(hash, password));
    }

    protected Uni<Void> validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new BadRequestException(INVALID_PASSWORD_STRENGTH);
        } else if (password.length() > 12) {
            return Uni.createFrom().voidItem();
        }

        if (!containsUpperCaseLetter.matcher(password).find()) {
            throw new BadRequestException(INVALID_PASSWORD_STRENGTH);
        }
        if (!containsLowerCaseLetter.matcher(password).find()) {
            throw new BadRequestException(INVALID_PASSWORD_STRENGTH);
        }

        if (!containsDigit.matcher(password).find() && !containsSpecialCharacter.matcher(password).find()) {
            throw new BadRequestException(INVALID_PASSWORD_STRENGTH);
        }

        return Uni.createFrom().voidItem();
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

    public Uni<Void> validateSetPasswordToken(String token) {
        return passwordTokenRepository.getByToken(token).call(passwordToken -> {
            Long minutesPassed = Duration.between(passwordToken.timestamp, Instant.now()).toMinutes();
            if (minutesPassed > EXPIRED_AFTER_MINUTES) {
                throw new BadRequestException();
            }
            return Uni.createFrom().voidItem();
        }).replaceWithVoid();
    }

    public Uni<Void> validateSetPasswordTokenNotCreated(String username) {
        return passwordTokenRepository.getByUsername(username)
                .call(passwordToken -> {
                    if (passwordToken != null) {
                        Long minutesPassed = Duration.between(passwordToken.timestamp, Instant.now()).toMinutes();
                        if (minutesPassed <= EXPIRED_AFTER_MINUTES) {
                            throw new BadRequestException();
                        }
                    }
                    return Uni.createFrom().voidItem();
                }).replaceWithVoid();
    }
}