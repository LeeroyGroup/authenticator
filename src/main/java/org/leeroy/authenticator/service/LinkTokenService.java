package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;

public interface LinkTokenService {

    Uni<Void> createSetPasswordToken(String username);

    boolean hasTokenExpired(String token);

    boolean hasTokenBeenUsed(String token);

    boolean isTokenExist(String token);
}
