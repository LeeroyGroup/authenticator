package org.leeroy.authenticator.service.impl;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.leeroy.authenticator.exception.InvalidLoginAttemptException;
import org.leeroy.authenticator.exception.WaitBeforeTryingLoginAgainException;
import org.leeroy.authenticator.model.Account;
import org.leeroy.authenticator.model.BlockedAccess;
import org.leeroy.authenticator.repository.AccountRepository;
import org.leeroy.authenticator.resource.request.AuthenticateRequest;
import org.leeroy.authenticator.service.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class AccountServiceImpl implements AccountService {

    @Inject
    BlockedAccessService blockedIPService;

    @Inject
    LoginAttemptService loginAttemptService;

    @Inject
    PasswordService passwordService;

    @Inject
    EmailService emailService;

    @Inject
    BlockedAccessService blockedAccessService;

    @Inject
    AccountRepository accountRepository;


    private static final String INVALID_EMAIL = "Invalid email";
    private static final String INVALID_PASSWORD_STRENGTH = "Invalid password strength";
    private static final String INVALID_LOGIN_ATTEMPT = "Invalid attempt login";
    private static final String USERNAME_ALREADY_EXIST = "Username already exist";

    private static final String FORGOT_PASSWORD_ATTEMPT_MESSAGE = "We sent you a link by e-mail so you can set the password";
    private static final String BLOCKED_EXCEPTION_MESSAGE = "You have to wait a while before you try again";

    @Override
    public Uni<Object> authenticate(AuthenticateRequest authenticateRequest) throws InvalidLoginAttemptException,
            WaitBeforeTryingLoginAgainException {

        AtomicReference<Uni<Object>> accountId = null;

        blockedIPService.isBlocked(authenticateRequest.getIpAddress(), authenticateRequest.getDevice())
                .onItem()
                .invoke(Unchecked.consumer(isBlocked -> {
                    if (isBlocked) {
                        Log.error(INVALID_LOGIN_ATTEMPT);
                        throw new WaitBeforeTryingLoginAgainException();
                    }
                }))
                .invoke(() -> isUsernameAndPasswordValid(authenticateRequest.getUsername(), authenticateRequest.getPassword())
                        .onItem().invoke(Unchecked.consumer(isUsernameAndPasswordValid -> {
                            if (!isUsernameAndPasswordValid) {
                                loginAttemptService.getLoginAttempts(authenticateRequest.getIpAddress(), authenticateRequest.getDevice())
                                        .onItem()
                                        .transform(count -> count > 15)
                                        .onItem()
                                        .invoke(isBlockedLoginAttempt -> {
                                            if (isBlockedLoginAttempt) {
                                                blockedAccessService.blockIP(BlockedAccess.builder()
                                                        .ipAddress(authenticateRequest.getIpAddress())
                                                        .device(authenticateRequest.getDevice())
                                                        .build());
                                            }
                                        })
                                        .invoke(() -> {
                                            loginAttemptService.createLoginAttempt(authenticateRequest.getIpAddress(), authenticateRequest.getDevice(),
                                                    authenticateRequest.getChannel(),
                                                    authenticateRequest.getClient(),
                                                    authenticateRequest.getUsername());
                                        });

                                throw new InvalidLoginAttemptException();
                            }
                        }))
                )
                .invoke(() -> {
                    loginAttemptService.createLoginAttempt(authenticateRequest.getIpAddress(), authenticateRequest.getDevice(),
                            authenticateRequest.getChannel(),
                            authenticateRequest.getClient(),
                            authenticateRequest.getUsername());

                    accountId.set(accountRepository.find("username", authenticateRequest.getUsername())
                            .firstResult()
                            .onItem()
                            .ifNotNull()
                            .transform(account -> account.id));
                });

        return accountId.get();
    }

    @Override
    public Uni<Void> forgotPassword(String ipAddress, String device, String username) {
        return blockedIPService.isBlocked(ipAddress, device).onItem().invoke(isBlocked -> {
                    if (isBlocked) {
                        Log.error(BLOCKED_EXCEPTION_MESSAGE);
                        throw new BadRequestException(BLOCKED_EXCEPTION_MESSAGE);
                    }
                })
                .onItem().call(() -> {
                            return accountRepository.hasUser(username).invoke(userExists -> {
                                        if (!userExists) {
                                            Log.error("Invalid attempt forgot password");
                                            throw new BadRequestException();
                                        }
                                    }).chain(() -> existSetPasswordLink(username)).invoke(existSetPasswordLink -> {
                                        if (!existSetPasswordLink) {
                                            Log.error("Invalid attempt forgot password");
                                            throw new BadRequestException();
                                        }
                                    })
                                    .invoke(() -> Log.info("Login attempt by " + ipAddress + " :" + device))
                                    .call(() -> sendSetPasswordEmail(username))
                                    .onFailure().call(() -> {
                                        Log.error("Invalid attempt forgot password");
                                        return loginAttemptService.createLoginAttempt(ipAddress, device, "", "", username)
                                                .chain(() -> loginAttemptService.getLoginAttempts(ipAddress, device))
                                                .onItem().invoke(attempts -> {
                                                    if (attempts > 10) {
                                                        blockedAccessService.blockIP(null);
                                                    }
                                                });
                                    });
                        }
                )
                .onItemOrFailure().transformToUni((item, failure) -> Uni.createFrom().voidItem());
    }

    private Uni<Void> sendSetPasswordEmail(String username) {
        return passwordService.createSetPasswordToken(username)
                .chain(token -> getSetPasswordEmailContent(token))
                .call(emailContent -> emailService.sendEmail(username, emailContent))
                .chain(item -> Uni.createFrom().voidItem());
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {

    }

    @Override
    public Uni<String> createAccount(String ipAddress, String device, String username, String password) {
        return Uni.createFrom().voidItem()
                .call(item -> validateNotBlocked(ipAddress, device))
                .chain(() -> {
                    return Uni.createFrom().voidItem()
                            .call(item -> validateUsernameFormat(username))
                            .call(item -> validatePasswordStrength(password))
                            .call(item -> validateUsernameNotTaken(username))
                            .onItem().transformToUni(item -> {
                                String id = UUID.randomUUID().toString();
                                Account account = Account.builder().id(id).username(username).password(password).build();
                                return accountRepository.persist(account).onItem().transform(entity -> entity.getId());
                            });
                });
    }

    @Override
    public Uni<String> createAccount(String ipAddress, String device, String username) {
        String password = UUID.randomUUID().toString();
        return createAccount(ipAddress, device, username, password)
                .onItem().call(item -> sendSetPasswordEmail(username));
    }

    private Uni<Void> validateNotBlocked(String ipAddress, String device) {
        return blockedIPService.isBlocked("ipAddress", "device").onItem().invoke(isBlocked -> {
            if (isBlocked) {
                Log.error(BLOCKED_EXCEPTION_MESSAGE);
                throw new BadRequestException(BLOCKED_EXCEPTION_MESSAGE);
            }
        }).chain(() -> Uni.createFrom().voidItem());
    }

    private Uni<Void> validateUsernameFormat(String username) {
        boolean isValidUsername = isUsernameValid(username);
        if (!isValidUsername) {
            throw new BadRequestException(INVALID_EMAIL);
        }
        return Uni.createFrom().voidItem();
    }

    private Uni<Void> validateUsernameNotTaken(String username) {
        return accountRepository.hasUser(username).invoke(hasUser -> {
            if (hasUser) {
                throw new BadRequestException(USERNAME_ALREADY_EXIST);
            }
        }).chain(item -> Uni.createFrom().voidItem());
    }

    private Uni<Void> validatePasswordStrength(String password) {
        boolean isValidPassword = passwordService.validatePasswordStrength(password);
        if (!isValidPassword) {
            throw new BadRequestException(INVALID_PASSWORD_STRENGTH);
        }
        return Uni.createFrom().voidItem();
    }

    @Override
    public void deleteAccount(String username, String password) {

    }

    private Uni<String> getSetPasswordEmailContent(String token) {
        return Uni.createFrom().item("Email:" + token);
    }

    private Uni<Boolean> isUsernameAndPasswordValid(String username, String password) {
        return Uni.createFrom().item(true);
    }

    private boolean isUsernameValid(String username) {
        return true;
    }

    private Uni<Boolean> existSetPasswordLink(String username) {
        return Uni.createFrom().item(true);
    }
}
