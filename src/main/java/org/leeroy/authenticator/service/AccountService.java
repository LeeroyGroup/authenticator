package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.exception.InvalidLoginAttemptException;
import org.leeroy.authenticator.exception.WaitBeforeTryingLoginAgainException;
import org.leeroy.authenticator.resource.request.AuthenticateRequest;

public interface AccountService {

    Uni<Long> authenticate(AuthenticateRequest authenticateRequest) throws InvalidLoginAttemptException,
            WaitBeforeTryingLoginAgainException;

    Uni<Void> forgotPassword(String ipAddress, String device, String username, String password);

    void changePassword(String username, String oldPassword, String newPassword);

    void createAccount(String username, String password);

    void createAccount(String username);

    void deleteAccount(String username, String password);
}
