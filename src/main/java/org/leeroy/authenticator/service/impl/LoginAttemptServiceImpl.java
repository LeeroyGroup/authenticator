package org.leeroy.authenticator.service.impl;

import org.leeroy.authenticator.service.LoginAttemptService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LoginAttemptServiceImpl implements LoginAttemptService {

    @Override
    public int getLoginAttempt(String ipAddress, String device) {
        return 0;
    }

    @Override
    public void createLoginAttempt() {

    }
}
