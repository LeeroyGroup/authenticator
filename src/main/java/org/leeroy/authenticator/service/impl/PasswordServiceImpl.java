package org.leeroy.authenticator.service.impl;

import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.service.PasswordService;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;
import java.util.regex.Pattern;

@ApplicationScoped
public class PasswordServiceImpl implements PasswordService {
    @Override
    public Uni<String> hashPassword(String password) {
        return null;
    }

    private static Pattern containsUpperCaseLetter = Pattern.compile("[A-Z]+");
    private static Pattern containsLowerCaseLetter = Pattern.compile("[a-z]+");
    private static Pattern containsDigit = Pattern.compile("[0-9]+");
    private static Pattern containsSpecialCharacter = Pattern.compile("[.#?!@$%^&*_-]+");

    @Override
    public boolean validatePasswordStrength(String password) {
        if (password.length() < 8) {
            return false;
        }
        else if (password.length() > 12) {
            return true;
        }

        if (!containsUpperCaseLetter.matcher(password).find()){
            return false;
        }
        if (!containsLowerCaseLetter.matcher(password).find()){
            return false;
        }

        return containsDigit.matcher(password).find() || containsSpecialCharacter.matcher(password).find();
    }

    @Override
    public Uni<String> createSetPasswordToken(String username) {
        String token = UUID.randomUUID().toString() + UUID.randomUUID().toString();
        return Uni.createFrom().item(token);
    }

    @Override
    public Uni<Void> isSetPasswordTokenValid(String username, String token) {
        return null;
    }
}
