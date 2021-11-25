package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;

public interface EmailService {

    Uni<Void> sendEmail(String email, String content);
}
