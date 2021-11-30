package org.leeroy.authenticator.service.impl;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.Account;
import org.leeroy.authenticator.repository.PasswordTokenRepository;
import org.leeroy.authenticator.resource.ClientID;
import org.leeroy.authenticator.service.AccountServiceBase;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class AccountService extends AccountServiceBase {

    @Inject
    protected PasswordTokenRepository passwordTokenRepository;

    public Uni<String> authenticate(ClientID clientID, String username, String password) {
        return Uni.createFrom().voidItem()
                .call(() -> super.validateNotBlocked(clientID))
                .call(() -> super.validateUsernamePassword(username, password))
                .onFailure().call(() -> createAttempt(clientID, username, "createAccount", false))
                .call(() -> createAttempt(clientID, username, "createAccount", true))
                .chain(() -> accountRepository.findByUsername(username))
                .map(account -> account.id.toString());
    }

    public Uni<Void> forgotPassword(ClientID clientID, String username) {
        return Uni.createFrom().voidItem()
                .call(item -> validateNotBlocked(clientID))
                .call(() -> {
                    return validateUsernameExist(username)
                            .call(() -> validatePasswordLink(username))
                            .invoke(() -> Log.info("Login attempt by " + clientID.ipAddress + " :" + clientID.device))
                            .call(() -> sendSetPasswordEmail(username))
                            .onFailure().call(() -> {
                                Log.error("Invalid attempt forgot password");
                                return attemptService.createAttempt(clientID, username, "forgotPassword", false)
                                        .chain(() -> attemptService.getAttempts(clientID))
                                        .onItem().invoke(attempts -> {
                                            if (attempts > 10) {
                                                blockedAccessService.block(clientID, "forgotPassword");
                                            }
                                        });
                            });
                })
                .onItemOrFailure().transformToUni((item, failure) -> Uni.createFrom().voidItem());
    }

    public Uni<String> createAccount(ClientID clientID, String username, String password) {
        return Uni.createFrom().voidItem()
                .call(item -> validateNotBlocked(clientID))
                .chain(() -> {
                    return Uni.createFrom().voidItem()
                            .call(item -> validateUsernameFormat(username))
                            .call(item -> validatePasswordStrength(password))
                            .call(item -> validateUsernameNotTaken(username))
                            .onItem().transformToUni(item -> {
                                Account account = new Account();
                                account.username = username;
                                account.password = password;
                                return accountRepository.persist(account).onItem().transform(entity -> entity.id.toString());
                            });
                });
    }

    public Uni<String> createAccount(ClientID clientID, String username) {
        String password = UUID.randomUUID().toString();
        return createAccount(clientID, username, password)
                .onItem().call(item -> sendSetPasswordEmail(username));
    }

    public Uni<Void> changePassword(ClientID clientID, String username, String currentPassword, String newPassword) {
        return validateUsernamePassword(username, currentPassword)
                .call(() -> passwordService.validatePasswordStrength(newPassword))
                .call(() -> accountRepository.setPassword(username, newPassword))
                .chain(() -> Uni.createFrom().voidItem());
    }

    @Override
    public Uni<Void> setPassword(ClientID clientID, String token, String password) {
        return passwordService.isSetPasswordTokenValid(token)
                .chain(() -> passwordTokenRepository.getUsernameByToken(token))
                .chain(username -> accountRepository.setPassword(username, password))
                .chain(() -> Uni.createFrom().voidItem());

    }

    public Uni<Void> deleteAccount(ClientID clientID, String username, String password) {
        return validateUsernamePassword(username, password)
                .call(() -> accountRepository.deleteByUsername(username))
                .chain(() -> Uni.createFrom().voidItem());
    }
}
