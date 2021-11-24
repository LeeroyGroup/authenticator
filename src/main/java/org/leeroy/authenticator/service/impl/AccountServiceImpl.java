package org.leeroy.authenticator.service.impl;

import io.quarkus.logging.Log;
import org.leeroy.authenticator.exception.InvalidLoginAttemptException;
import org.leeroy.authenticator.exception.WaitBeforeTryingLoginAgainException;
import org.leeroy.authenticator.model.BlockedAccess;
import org.leeroy.authenticator.repository.AccountRepository;
import org.leeroy.authenticator.resource.request.AuthenticateRequest;
import org.leeroy.authenticator.service.AccountService;
import org.leeroy.authenticator.service.BlockedAccessService;
import org.leeroy.authenticator.service.LoginAttemptService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AccountServiceImpl implements AccountService {

    @Inject
    BlockedAccessService blockedIPService;

    @Inject
    LoginAttemptService loginAttemptService;

    @Inject
    BlockedAccessService blockedAccessService;

    @Inject
    AccountRepository accountRepository;

    @Override
    public String authenticate(AuthenticateRequest authenticateRequest) throws InvalidLoginAttemptException,
            WaitBeforeTryingLoginAgainException {
        if (blockedIPService.isBlocked(authenticateRequest.getIpAddress(), authenticateRequest.getDevice())) {
            Log.error("Invalid attempt login");
            throw new WaitBeforeTryingLoginAgainException();

        } else {
            if (isUsernameAndPasswordValid(authenticateRequest.getUsername(), authenticateRequest.getPassword())) {

                loginAttemptService.createLoginAttempt();

                String accountId = "";
                // TODO extract the accountId
                accountRepository.find("username", authenticateRequest.getUsername()).firstResult();

                return accountId;

            } else {
                if (loginAttemptService.getLoginAttempt(authenticateRequest.getIpAddress(),
                        authenticateRequest.getDevice()) > 15) {
                    blockedAccessService.blockIP(BlockedAccess.builder()
                            .ipAddress(authenticateRequest.getIpAddress())
                            .device(authenticateRequest.getDevice())
                            .build());
                }

                loginAttemptService.createLoginAttempt();

                throw new InvalidLoginAttemptException();
            }
        }
    }

    @Override
    public void forgotPassword() {

    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {

    }

    @Override
    public void createAccount(String username, String password) {

    }

    @Override
    public void createAccount(String username) {

    }

    @Override
    public void deleteAccount(String username, String password) {

    }

    private boolean isUsernameAndPasswordValid(String username, String password) {
        return false;
    }

    private boolean isUsernameValid(String username) {
        return false;
    }

}
