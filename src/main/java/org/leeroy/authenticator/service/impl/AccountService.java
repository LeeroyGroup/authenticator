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

@ApplicationScoped
public class AccountService extends AccountServiceBase {

    public Uni<Object> authenticate(AuthenticateRequest authenticateRequest) {
        return Uni.createFrom().voidItem()
                .call(item -> super.validateNotBlocked(authenticateRequest.getIpAddress(), authenticateRequest.getDevice()))
                .chain(item -> super.validateUsernamePassword(authenticateRequest.getUsername(), authenticateRequest.getPassword(),
                        authenticateRequest.getIpAddress(), authenticateRequest.getDevice(), authenticateRequest.getClient(), authenticateRequest.getChannel()))
                .chain(() -> loginAttemptService.createLoginAttempt(authenticateRequest.getIpAddress(),
                        authenticateRequest.getDevice(), authenticateRequest.getChannel(),
                        authenticateRequest.getClient(), authenticateRequest.getUsername()))
                .chain(() -> super.getAccountId(authenticateRequest.getUsername()));
    }

    public Uni<Void> forgotPassword(String ipAddress, String device, String username) {
        return blockedAccessService.isBlocked(ipAddress, device).onItem().invoke(isBlocked -> {
                    if (isBlocked) {
                        Log.error(super.BLOCKED_EXCEPTION_MESSAGE);
                        throw new BadRequestException(BLOCKED_EXCEPTION_MESSAGE);
                    }
                })
                .onItem().call(() -> {
                            return accountRepository.hasUser(username).invoke(userExists -> {
                                        if (!userExists) {
                                            Log.error("Invalid attempt forgot password");
                                            throw new BadRequestException();
                                        }
                                    }).chain(() -> super.existSetPasswordLink(username)).invoke(existSetPasswordLink -> {
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

    public Uni<String> createAccount(String ipAddress, String device, String username, String password) {
        return Uni.createFrom().voidItem()
                .call(item -> validateNotBlocked(ipAddress, device))
                .chain(() -> {
                    return Uni.createFrom().voidItem()
                            .call(item -> validateUsernameFormat(username))
                            .call(item -> validatePasswordStrength(password))
                            .call(item -> validateUsernameNotTaken(username))
                            .onItem().transformToUni(item -> {
                                Account account = Account.builder().username(username).password(password).build();
                                return accountRepository.persist(account).onItem().transform(entity -> entity.id.toString());
                            });
                });
    }

    public Uni<String> createAccount(String ipAddress, String device, String username) {
        String password = UUID.randomUUID().toString();
        return createAccount(ipAddress, device, username, password)
                .onItem().call(item -> sendSetPasswordEmail(username));
    }

    public void changePassword(String username, String oldPassword, String newPassword) {

    }

    public void deleteAccount(String username, String password) {

    }

}
