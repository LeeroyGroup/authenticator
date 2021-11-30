package org.leeroy.authenticator.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.repository.AccountRepository;
import org.leeroy.authenticator.resource.ClientID;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

public abstract class AccountServiceBase {

    @Inject
    protected AccountRepository accountRepository;
    @Inject
    protected BlockedAccessService blockedAccessService;
    @Inject
    protected AttemptService attemptService;
    @Inject
    protected PasswordService passwordService;
    @Inject
    protected EmailService emailService;

    protected static final String BLOCKED_EXCEPTION_MESSAGE = "You have to wait a while before you try again";
    private static final String USERNAME_ALREADY_EXIST = "Username already exist";
    private static final String INVALID_EMAIL = "Invalid email";
    private static final String INVALID_PASSWORD_STRENGTH = "Invalid password strength";
    protected static final String INVALID_LOGIN_ATTEMPT = "Invalid attempt login";
    private static final String FORGOT_PASSWORD_ATTEMPT_MESSAGE = "We sent you a link by e-mail so you can set the password";

    public abstract Uni<String> authenticate(ClientID clientID, String username, String password);

    public abstract Uni<Void> forgotPassword(ClientID clientID, String username);

    public abstract Uni<Void> changePassword(ClientID clientID, String username, String currentPassword, String newPassword);

    public abstract Uni<Void> setPassword(ClientID clientID, String token, String password);

    public abstract Uni<String> createAccount(ClientID clientID, String username, String password);

    public abstract Uni<String> createAccount(ClientID clientID, String username);

    public abstract Uni<Void> deleteAccount(ClientID clientID, String username, String password);

    protected Uni<Void> sendSetPasswordEmail(String username) {
        return passwordService.createSetPasswordToken(username)
                .chain(token -> getSetPasswordEmailContent(token))
                .call(emailContent -> emailService.sendEmail(username, emailContent))
                .chain(item -> Uni.createFrom().voidItem());
    }

    protected Uni<Void> validateNotBlocked(ClientID clientID) {
        return blockedAccessService.isBlocked(clientID).onItem().invoke(isBlocked -> {
            if (isBlocked) {
                Log.error(BLOCKED_EXCEPTION_MESSAGE);
                throw new BadRequestException(BLOCKED_EXCEPTION_MESSAGE);
            }
        }).chain(() -> Uni.createFrom().voidItem());
    }

    private Uni<String> getSetPasswordEmailContent(String token) {
        return Uni.createFrom().item("Email:" + token);
    }

    protected Uni<Void> validateUsernameFormat(String username) {
        boolean isValidUsername = isUsernameValid(username);
        if (!isValidUsername) {
            throw new BadRequestException(INVALID_EMAIL);
        }
        return Uni.createFrom().voidItem();
    }

    protected Uni<Void> validateUsernamePassword(String username, String password) {
        return Uni.createFrom().voidItem();
    }

    protected Uni<Void> createAttempt(ClientID clientID, String username, String attemptType, boolean valid) {
        if (!valid) {
            attemptService.getAttempts(clientID)
                    .onItem()
                    .transform(count -> count > 15)
                    .onItem()
                    .invoke(isBlockedLoginAttempt -> {
                        if (isBlockedLoginAttempt) {
                            blockedAccessService.block(clientID, attemptType);
                        }
                    })
                    .invoke(() -> {
                        attemptService.createAttempt(clientID, username, attemptType, valid);
                    });
            throw new BadRequestException(INVALID_LOGIN_ATTEMPT);
        }
        return Uni.createFrom().voidItem();
    }

    protected Uni<Void> validateUsernameNotTaken(String username) {
        return accountRepository.hasUser(username).invoke(hasUser -> {
            if (hasUser) {
                throw new BadRequestException(USERNAME_ALREADY_EXIST);
            }
        }).chain(item -> Uni.createFrom().voidItem());
    }

    protected Uni<Void> validateUsernameExist(String username) {
        return accountRepository.hasUser(username).invoke(hasUser -> {
            if (!hasUser) {
                throw new BadRequestException(USERNAME_ALREADY_EXIST);
            }
        }).chain(item -> Uni.createFrom().voidItem());
    }

    protected Uni<Void> validatePasswordStrength(String password) {
        return passwordService.validatePasswordStrength(password).invoke(isValidPassword -> {
            if (!isValidPassword) {
                throw new BadRequestException(INVALID_PASSWORD_STRENGTH);
            }
        }).chain(() -> Uni.createFrom().voidItem());
    }

    private Uni<Boolean> isUsernameAndPasswordExist(String username, String password) {
        return passwordService.hashPassword(password)
                .chain(hashPassword -> {
                    return accountRepository.findByUsernameAndPassword(username, hashPassword)
                            .onItem()
                            .ifNotNull()
                            .transform(account -> account.id != null);
                });
    }

    private boolean isUsernameValid(String username) {
        return emailService.isValidEmail(username);
    }

    protected Uni<Boolean> existSetPasswordLink(String username) {
        return Uni.createFrom().item(true);
    }

    public Uni<Void> validatePasswordLink(String username) {
        return existSetPasswordLink(username).invoke(existSetPasswordLink -> {
            if (!existSetPasswordLink) {
                Log.error("Invalid attempt forgot password");
                throw new BadRequestException();
            }
        }).chain(() -> Uni.createFrom().voidItem());
    }
}
