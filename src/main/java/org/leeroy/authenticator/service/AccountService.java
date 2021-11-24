package org.leeroy.authenticator.service;

import org.leeroy.authenticator.exception.InvalidLoginAttemptException;
import org.leeroy.authenticator.exception.WaitBeforeTryingLoginAgainException;
import org.leeroy.authenticator.resource.request.AuthenticateRequest;

public interface AccountService {

    String authenticate(AuthenticateRequest authenticateRequest) throws InvalidLoginAttemptException, WaitBeforeTryingLoginAgainException;

    void forgotPassword();

    void changePassword(String username, String oldPassword, String newPassword);

    void createAccount(String username, String password);

    void createAccount(String username);

    void deleteAccount(String username, String password);
}
