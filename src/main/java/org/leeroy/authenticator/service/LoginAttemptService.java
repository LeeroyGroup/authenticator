package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;

public interface LoginAttemptService {

    Uni<Long> getLoginAttempts(String ipAddress, String device);

    Uni<Void> createLoginAttempt(String ipAddress, String device, String channel, String client, String username);
}
