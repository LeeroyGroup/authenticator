package org.leeroy.authenticator.service;

import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;
import java.util.regex.Pattern;

@ApplicationScoped
public class EmailService {

    private static final Pattern validEmailPattern = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\." +
            "[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
            "A-Z]{2,7}$");

    private static final String INVALID_EMAIL = "Invalid email";

    public boolean isValidEmail(String email) {
        if (email == null)
            return false;
        return validEmailPattern.matcher(email).matches();
    }

    protected Uni<Void> validateEmailFormat(String username) {
        boolean isValidUsername = isValidEmail(username);
        if (!isValidUsername) {
            throw new BadRequestException(INVALID_EMAIL);
        }
        return Uni.createFrom().voidItem();
    }

    public Uni<Void> sendEmail(String email, String content) {
        return Uni.createFrom().voidItem();
    }


}
