package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.repository.AccountRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;

@ApplicationScoped
public class AccountService {

    @Inject
    AccountRepository accountRepository;
    @Inject
    PasswordService passwordService;

    private static final String USERNAME_ALREADY_EXIST = "Username already exist";
    private static final String USERNAME_DOES_NOT_EXIST = "Username doesn't exist";
    private static final String INVALID_LOGIN_ATTEMPT = "Invalid login attempt";

    public Uni<String> validateCredentials(String username, String password) {
        return accountRepository.findByUsername(username)
                .onItem().ifNull().failWith(() -> {
                    throw new BadRequestException(INVALID_LOGIN_ATTEMPT);
                })
                .call(account -> {
                    return passwordService.verifyPassword(account.password, password)
                            .invoke(isPasswordValid -> {
                                if (!isPasswordValid) {
                                    throw new BadRequestException(INVALID_LOGIN_ATTEMPT);
                                }
                            });
                })
                .map(account -> account.id.toString());
    }

    public Uni<Void> validateUsernameExist(String username) {
        return accountRepository.hasUser(username).invoke(hasUser -> {
            if (!hasUser) {
                throw new BadRequestException(USERNAME_DOES_NOT_EXIST);
            }
        }).chain(item -> Uni.createFrom().voidItem());
    }

    public Uni<Void> validateUsernameNotExist(String username) {
        return accountRepository.hasUser(username).invoke(hasUser -> {
            if (hasUser) {
                throw new BadRequestException(USERNAME_ALREADY_EXIST);
            }
        }).chain(item -> Uni.createFrom().voidItem());
    }
}
