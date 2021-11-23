package org.leeroy.authenticator.service.impl;

import org.leeroy.authenticator.repository.BlockedIPRepository;
import org.leeroy.authenticator.service.AccountService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class AccountServiceImpl implements AccountService {

    @Inject
    BlockedIPRepository blockedIPRepository;

    @Override
    public void authenticate(String ipAddress, String device) {
        // Verify if the IP is blocked
    }
}
