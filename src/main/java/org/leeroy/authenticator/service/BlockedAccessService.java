package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.resource.ClientID;

public interface BlockedAccessService {

    Uni<Boolean> isBlocked(ClientID clientID);

    Uni<Void> block(ClientID clientID, String reason);

}
