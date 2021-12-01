package org.leeroy.authenticator.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.Account;
import org.leeroy.authenticator.repository.AccountRepository;
import org.leeroy.authenticator.repository.PasswordTokenRepository;
import org.leeroy.authenticator.resource.ClientID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class AuthenticatorService {

    @Inject
    AccountRepository accountRepository;
    @Inject
    BlockedAccessService blockedAccessService;
    @Inject
    AttemptService attemptService;
    @Inject
    PasswordService passwordService;
    @Inject
    EmailService emailService;
    @Inject
    AccountService accountService;

    @Inject
    private PasswordTokenRepository passwordTokenRepository;

    private static final String FORGOT_PASSWORD_ATTEMPT_MESSAGE = "We sent you a link by e-mail so you can set the password";
    private final static String CREATE_ACCOUNT_ATTEMPT = "createAccount";

    public Uni<String> authenticateAccount(ClientID clientID, String username, String password) {
        return accountService.validateCredentials(username, password)
                .call(() -> createAttempt(clientID, username, CREATE_ACCOUNT_ATTEMPT, true))
                .onFailure().call(() -> createAttempt(clientID, username, CREATE_ACCOUNT_ATTEMPT, false));
    }

    public Uni<Void> forgotPassword(ClientID clientID, String username) {
        return accountService.validateUsernameExist(username)
                .call(() -> passwordService.validateSetPasswordTokenNotCreated(username))
                .invoke(() -> Log.info("Login attempt by " + clientID.ipAddress + " :" + clientID.device))
                .call(() -> sendSetPasswordEmail(username))
                .onFailure().call(() -> {
                    Log.error("Invalid attempt forgot password");
                    return attemptService.createAttempt(clientID, username, "forgotPassword", false)
                            .chain(() -> attemptService.getAttempts(clientID, 15))
                            .onItem().call(attempts -> {
                                if (attempts > 10) {
                                    return blockedAccessService.block(clientID, "forgotPassword");
                                }

                                return Uni.createFrom().voidItem();
                            });
                });
    }

    public Uni<Void> changePassword(ClientID clientID, String username, String currentPassword, String newPassword) {
        return accountService.validateCredentials(username, currentPassword)
                .call(() -> passwordService.validatePasswordStrength(newPassword))
                .call(() -> accountRepository.setPassword(username, newPassword))
                .replaceWithVoid();
    }

    public Uni<Void> setPassword(ClientID clientID, String token, String password) {
        return passwordService.validateSetPasswordToken(token)
                .chain(() -> passwordTokenRepository.getUsernameByToken(token))
                .chain(username -> accountRepository.setPassword(username, password))
                .replaceWithVoid();

    }

    public Uni<String> createAccount(ClientID clientID, String username, String password) {
        return Uni.createFrom().voidItem()
                .call(item -> emailService.validateEmailFormat(username))
                .call(item -> passwordService.validatePasswordStrength(password))
                .call(item -> accountService.validateUsernameNotExist(username))
                .chain(() -> passwordService.hashPassword(password))
                .chain(hashedPassword -> {
                    Account account = new Account();
                    account.username = username;
                    account.password = hashedPassword;
                    return accountRepository.persist(account).onItem().transform(entity -> entity.id.toString());
                });
    }

    public Uni<String> createAccount(ClientID clientID, String username) {
        String password = UUID.randomUUID().toString();
        return createAccount(clientID, username, password)
                .onItem().call(item -> sendSetPasswordEmail(username));
    }

    public Uni<Void> deleteAccount(ClientID clientID, String username, String password) {
        return accountService.validateCredentials(username, password)
                .call(() -> accountRepository.deleteByUsername(username))
                .replaceWithVoid();
    }

    public Uni<Void> createAttempt(ClientID clientID, String username, String attemptType, boolean valid) {
        if (valid) {
            return attemptService.createAttempt(clientID, username, attemptType, valid);
        }

        return attemptService.createAttempt(clientID, username, attemptType, valid)
                .chain(() -> attemptService.getAttempts(clientID, 15))
                .map(count -> count > 15)
                .invoke(isBlockedLoginAttempt -> {
                    if (isBlockedLoginAttempt) {
                        blockedAccessService.block(clientID, attemptType);
                    }
                }).replaceWithVoid();
    }

    protected Uni<Void> sendSetPasswordEmail(String username) {
        return passwordService.createSetPasswordToken(username)
                .chain(token -> getSetPasswordEmailContent(token))
                .call(emailContent -> emailService.sendEmail(username, emailContent))
                .chain(item -> Uni.createFrom().voidItem());
    }

    private Uni<String> getSetPasswordEmailContent(String token) {
        return Uni.createFrom().item("Email:" + token);
    }
}
