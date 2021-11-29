package org.leeroy.authenticator.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.exception.InvalidLoginAttemptException;
import org.leeroy.authenticator.exception.WaitBeforeTryingLoginAgainException;
import org.leeroy.authenticator.model.BlockedAccess;
import org.leeroy.authenticator.repository.AccountRepository;
import org.leeroy.authenticator.resource.request.AuthenticateRequest;

import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

public abstract class AccountServiceBase {

    @Inject
    protected AccountRepository accountRepository;
    @Inject
    protected BlockedAccessService blockedAccessService;
    @Inject
    protected LoginAttemptService loginAttemptService;
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

    public abstract Uni<Object> authenticate(AuthenticateRequest authenticateRequest) throws InvalidLoginAttemptException,
            WaitBeforeTryingLoginAgainException;

    public abstract Uni<Void> forgotPassword(String ipAddress, String device, String username);

    public abstract Uni<Void> changePassword(String username, String oldPassword, String newPassword);

    public abstract Uni<Void> setPassword(String token, String password);

    public abstract Uni<String> createAccount(String ipAddress, String device, String username, String password);

    public abstract Uni<String> createAccount(String ipAddress, String device, String username);

    public abstract Uni<Void> deleteAccount(String username, String password);

    protected Uni<Void> sendSetPasswordEmail(String username) {
        return passwordService.createSetPasswordToken(username)
                .chain(token -> getSetPasswordEmailContent(token))
                .call(emailContent -> emailService.sendEmail(username, emailContent))
                .chain(item -> Uni.createFrom().voidItem());
    }

    protected Uni<Void> validateNotBlocked(String ipAddress, String device) {
        return blockedAccessService.isBlocked("ipAddress", "device").onItem().invoke(isBlocked -> {
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

    protected Uni<Void> createAttempt(String ipAddress, String device, String client, String channel, String username, boolean valid) {
        if (!valid) {
            loginAttemptService.getLoginAttempts(ipAddress, device)
                    .onItem()
                    .transform(count -> count > 15)
                    .onItem()
                    .invoke(isBlockedLoginAttempt -> {
                        if (isBlockedLoginAttempt) {
                            blockedAccessService.blockIP(BlockedAccess.builder()
                                    .ipAddress(ipAddress)
                                    .device(device)
                                    .build());
                        }
                    })
                    .invoke(() -> {
                        loginAttemptService.createLoginAttempt(ipAddress, device, channel, client, username);
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

    private boolean isUsernameAndPasswordValid(String username, String password) {
        return true;
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

    protected Uni<Object> getAccountId(String username) {
        return accountRepository.find("username", username)
                .firstResult()
                .onItem()
                .ifNotNull()
                .transform(account -> account.id.toString());
    }

}
