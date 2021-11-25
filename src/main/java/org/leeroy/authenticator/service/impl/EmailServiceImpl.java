package org.leeroy.authenticator.service.impl;

import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.service.EmailService;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {
    @Override
    public Uni<Void> sendEmail(String email, String content) {
        return Uni.createFrom().voidItem();
    }
}
