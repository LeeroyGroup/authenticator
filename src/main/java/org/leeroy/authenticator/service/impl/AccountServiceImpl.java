package org.leeroy.authenticator.service.impl;

import org.leeroy.authenticator.service.AccountService;
import org.leeroy.authenticator.service.BlockedAccessService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AccountServiceImpl implements AccountService {

    @Inject
    BlockedAccessService blockedIPService;

    @Override
    public void authenticate(String ipAddress, String device) {

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

    private boolean validateUsernameAndPassword(String username, String password) {
        return false;
    }

    private boolean validateUsername(String username) {
        return false;
    }

}
