package org.leeroy.authenticator.service;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.Account;
import org.leeroy.authenticator.repository.AccountRepository;
import org.leeroy.authenticator.repository.PasswordTokenRepository;
import org.leeroy.authenticator.resource.RequestID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
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

    private final static String AUTHENTICATE_ACCOUNT_ATTEMPT = "Authenticate Account";
    private final static String CREATE_ACCOUNT_ATTEMPT = "Create Account";
    private final static String DELETE_ACCOUNT_ATTEMPT = "Create Account";
    private final static String CHANGE_PASSWORD_ATTEMPT = "Change Password";
    private final static String FORGOT_PASSWORD_ATTEMPT = "Forgot Password";
    private final static String SET_PASSWORD_ATTEMPT = "Set Password";

    private final static int CREATE_ACCOUNT_MAX = 10;
    private final static int CREATE_ACCOUNT_TIMESPAN_DAYS = 1;
    private final static int MINUTES_IN_A_DAY = 1440;

    public Uni<String> authenticateAccount(RequestID requestID, String username, String password) {
        return accountService.validateCredentials(username, password)
                .onItemOrFailure().call((item, failure) -> createAttempt(requestID, username, AUTHENTICATE_ACCOUNT_ATTEMPT, item != null));
    }

    public Uni<Void> forgotPassword(RequestID requestID, String username) {
        return accountService.validateUsernameExist(username)
                .call(() -> passwordService.validateSetPasswordTokenNotCreated(username))
                .invoke(() -> Log.info("Login attempt by " + requestID.ipAddress + " :" + requestID.device))
                .call(() -> sendSetPasswordEmail(username))
                .onFailure().call(() -> {
                    Log.error("Invalid attempt forgot password");
                    return attemptService.createAttempt(requestID, username, FORGOT_PASSWORD_ATTEMPT, false)
                            .chain(() -> attemptService.getAttempts(requestID, FORGOT_PASSWORD_ATTEMPT, false, 15))
                            .call(attempts -> {
                                if (attempts > 10) {
                                    return blockedAccessService.block(requestID, FORGOT_PASSWORD_ATTEMPT);
                                }

                                return Uni.createFrom().voidItem();
                            });
                });
    }

    public Uni<Void> changePassword(RequestID requestID, String username, String currentPassword, String newPassword) {
        return accountService.validateCredentials(username, currentPassword)
                .call(() -> passwordService.validatePasswordStrength(newPassword))
                .call(() -> accountRepository.setPassword(username, newPassword))
                .onItemOrFailure().call((item, failure) -> createAttempt(requestID, username, CHANGE_PASSWORD_ATTEMPT, item != null))
                .replaceWithVoid();
    }

    public Uni<Void> setPassword(RequestID requestID, String token, String password) {
        return passwordService.validateSetPasswordToken(token)
                .chain(() -> passwordTokenRepository.getUsernameByToken(token))
                .call(username -> accountRepository.setPassword(username, password))
                .onItemOrFailure().call((username, failure) -> createAttempt(requestID, username, SET_PASSWORD_ATTEMPT, username != null))
                .replaceWithVoid();
    }

    public Uni<String> createAccount(RequestID requestID, String username, String password) {
        // Block if RequestID has been creating to many accounts in the specified time
        return attemptService.getAttempts(requestID, CREATE_ACCOUNT_ATTEMPT, true, CREATE_ACCOUNT_TIMESPAN_DAYS * MINUTES_IN_A_DAY)
                .map(count -> count >= CREATE_ACCOUNT_MAX)
                .call(shouldBlock -> {
                    if (shouldBlock) {
                        return blockedAccessService.block(requestID, CREATE_ACCOUNT_ATTEMPT)
                                .invoke(() -> {
                                    throw new BadRequestException("Can't create more accounts, have to wait");
                                });
                    }
                    return Uni.createFrom().voidItem();
                })
                .chain(() -> emailService.validateEmailFormat(username)
                        .call(item -> passwordService.validatePasswordStrength(password))
                        .call(item -> accountService.validateUsernameNotExist(username))
                        .chain(() -> passwordService.hashPassword(password))
                        .chain(hashedPassword -> {
                            Account account = new Account();
                            account.username = username;
                            account.password = hashedPassword;
                            return accountRepository.persist(account).onItem().transform(entity -> entity.id.toString());
                        })
                        .onItemOrFailure().call((item, failure) -> createAttempt(requestID, username, CREATE_ACCOUNT_ATTEMPT, item != null))
                );
    }

    public Uni<String> createAccount(RequestID requestID, String username) {
        String password = UUID.randomUUID().toString();
        return createAccount(requestID, username, password)
                .onItem().call(item -> sendSetPasswordEmail(username));
    }

    public Uni<Void> deleteAccount(RequestID requestID, String username, String password) {
        return accountService.validateCredentials(username, password)
                .call(() -> accountRepository.deleteByUsername(username))
                .onItemOrFailure().call((item, failure) -> createAttempt(requestID, username, DELETE_ACCOUNT_ATTEMPT, item != null))
                .replaceWithVoid();
    }

    public Uni<Void> createAttempt(RequestID requestID, String username, String attemptType, boolean successful) {
        if (successful) {
            return attemptService.createAttempt(requestID, username, attemptType, successful);
        }

        return attemptService.createAttempt(requestID, username, attemptType, successful)
                .chain(() -> attemptService.getAttempts(requestID, attemptType, successful, 15))
                .map(count -> count > 15)
                .invoke(isBlockedLoginAttempt -> {
                    if (isBlockedLoginAttempt) {
                        blockedAccessService.block(requestID, attemptType);
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
