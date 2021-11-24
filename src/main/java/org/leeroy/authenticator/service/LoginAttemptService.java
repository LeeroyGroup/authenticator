package org.leeroy.authenticator.service;

public interface LoginAttemptService {

    int getLoginAttempt(String ipAddress, String device);

    void createLoginAttempt();
}
