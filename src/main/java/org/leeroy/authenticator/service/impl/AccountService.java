package org.leeroy.authenticator.service.impl;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.model.Account;
import org.leeroy.authenticator.repository.PasswordTokenRepository;
import org.leeroy.authenticator.resource.request.AuthenticateRequest;
import org.leeroy.authenticator.service.AccountServiceBase;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.UUID;

@ApplicationScoped
public class AccountService extends AccountServiceBase {

    @Inject
    protected PasswordTokenRepository passwordTokenRepository;

    public Uni<Object> authenticate(AuthenticateRequest authenticateRequest) {
        return Uni.createFrom().voidItem()
                .call(item -> super.validateNotBlocked(authenticateRequest.getIpAddress(), authenticateRequest.getDevice()))
                .chain(item -> super.validateUsernamePassword(authenticateRequest.getUsername(), authenticateRequest.getPassword())
                .onFailure().call(() -> createAttempt(authenticateRequest.getIpAddress(), authenticateRequest.getDevice(), authenticateRequest.getClient(), authenticateRequest.getChannel(), authenticateRequest.getUsername(), false)))
                .chain(() -> loginAttemptService.createLoginAttempt(authenticateRequest.getIpAddress(),
                        authenticateRequest.getDevice(), authenticateRequest.getChannel(),
                        authenticateRequest.getClient(), authenticateRequest.getUsername()))
                .chain(() -> super.getAccountId(authenticateRequest.getUsername()));
    }

    public Uni<Void> forgotPassword(String ipAddress, String device, String username) {
        return Uni.createFrom().voidItem()
                .call(item -> validateNotBlocked(ipAddress, device))
                .call(() -> {
                    return validateUsernameExist(username)
                    .call(() -> validatePasswordLink(username))
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
                })
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

    public Uni<Void> changePassword(String username, String oldPassword, String newPassword) {
        return validateUsernamePassword(username, oldPassword)
                .call(() -> passwordService.validatePasswordStrength(newPassword))
                .call(() -> accountRepository.setPassword(username, newPassword))
                .chain(() -> Uni.createFrom().voidItem());
    }

    @Override
    public Uni<Void> setPassword(String token, String password) {
        return passwordService.isSetPasswordTokenValid(token)
                .chain(() -> passwordTokenRepository.getUsernameByToken(token))
                .chain(username -> accountRepository.setPassword(username, password))
                .chain(() -> Uni.createFrom().voidItem());

    }

    public Uni<Void> deleteAccount(String username, String password) {
        return validateUsernamePassword(username, password)
                .call(() -> accountRepository.deleteByUsername(username))
                .chain(() -> Uni.createFrom().voidItem());
    }

}
