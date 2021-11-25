package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.resource.request.AuthenticateRequest;

public interface LoginAttemptService {

    Uni<Long> getLoginAttempts(String ipAddress, String device);

    Uni<Void> createLoginAttempt(AuthenticateRequest authenticateRequest);
}
