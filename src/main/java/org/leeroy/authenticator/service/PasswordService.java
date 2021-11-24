package org.leeroy.authenticator.service;

public interface PasswordService {

    String hashPassword(String password);

    boolean validatePasswordStrength(String password);
}
