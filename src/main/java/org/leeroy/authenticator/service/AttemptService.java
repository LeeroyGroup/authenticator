package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.resource.ClientID;

public interface AttemptService {

    Uni<Long> getAttempts(ClientID clientID);

    Uni<Void> createAttempt(ClientID clientID, String username, String attemptType, boolean valid);
}
