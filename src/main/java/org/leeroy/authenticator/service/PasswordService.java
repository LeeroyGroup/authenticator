package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;

public interface PasswordService {

    Uni<String> hashPassword(String password);

    Uni<Boolean> validatePasswordStrength(String password);

    Uni<String> createSetPasswordToken(String username);

    Uni<Void> isSetPasswordTokenValid(String token);
}
