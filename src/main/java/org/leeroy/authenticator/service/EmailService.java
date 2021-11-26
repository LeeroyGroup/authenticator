package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;

public interface EmailService {

    boolean isValidEmail(String email);
    Uni<Void> sendEmail(String email, String content);
}
