package org.leeroy.authenticator.service;

public interface AccountService {

    void authenticate(String ipAddress, String device);

    void forgotPassword();

    void changePassword(String username, String oldPassword, String newPassword);

    void createAccount(String username, String password);

    void createAccount(String username);

    void deleteAccount(String username, String password);
}
