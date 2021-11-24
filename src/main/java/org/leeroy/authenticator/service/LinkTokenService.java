package org.leeroy.authenticator.service;

public interface LinkTokenService {

    void createPasswordToken();

    boolean hasTokenExpired(String token);

    boolean hasTokenBeenUsed(String token);

    boolean isTokenExist(String token);
}
