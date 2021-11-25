package org.leeroy.authenticator.service.impl;

import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.service.PasswordService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PasswordServiceImpl implements PasswordService {
    @Override
    public Uni<String> hashPassword(String password) {
        return null;
    }

    @Override
    public boolean validatePasswordStrength(String password) {
        return false;
    }

    @Override
    public Uni<String> createSetPasswordToken(String username) {
        return null;
    }

    @Override
    public Uni<Void> isSetPasswordTokenValid(String username, String token) {
        return null;
    }
}
