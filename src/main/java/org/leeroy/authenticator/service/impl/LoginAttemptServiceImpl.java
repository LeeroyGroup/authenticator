package org.leeroy.authenticator.service.impl;

import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;
import org.leeroy.authenticator.model.LoginAttempt;
import org.leeroy.authenticator.repository.LoginAttemptRepository;
import org.leeroy.authenticator.resource.request.AuthenticateRequest;
import org.leeroy.authenticator.service.LoginAttemptService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class LoginAttemptServiceImpl implements LoginAttemptService {

    @Inject
    LoginAttemptRepository loginAttemptRepository;

    private static final Logger LOG = Logger.getLogger(LoginAttemptServiceImpl.class);

    @Override
    public Uni<Long> getLoginAttempts(String ipAddress, String device) {
        return loginAttemptRepository.find("ipAddress = ?1 and device = ?2", ipAddress, device).count();
    }

    @Override
    public Uni<Void> createLoginAttempt(AuthenticateRequest authenticateRequest) {
        LoginAttempt attempt = LoginAttempt.builder()
                .ipAddress(authenticateRequest.getIpAddress())
                .device(authenticateRequest.getDevice())
                .channel(authenticateRequest.getChannel())
                .client(authenticateRequest.getClient())
                .username(authenticateRequest.getUsername())
                .build();

        LOG.log(Logger.Level.INFO, attempt);
        loginAttemptRepository.persist(attempt);

        return Uni.createFrom().voidItem();
    }
}
