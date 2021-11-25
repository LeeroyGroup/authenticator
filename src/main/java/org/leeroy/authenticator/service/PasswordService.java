package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;

public interface PasswordService {

    Uni<String> hashPassword(String password);

    boolean validatePasswordStrength(String password);

    Uni<String> createSetPasswordToken(String username);

    Uni<Void> isSetPasswordTokenValid(String username, String token);
}
