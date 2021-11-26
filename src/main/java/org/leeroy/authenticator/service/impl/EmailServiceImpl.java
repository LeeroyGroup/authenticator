package org.leeroy.authenticator.service.impl;

import io.smallrye.mutiny.Uni;
import org.leeroy.authenticator.service.EmailService;

import javax.enterprise.context.ApplicationScoped;
import java.util.regex.Pattern;

@ApplicationScoped
public class EmailServiceImpl implements EmailService {
    private static final Pattern validEmailPattern = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\."+
            "[a-zA-Z0-9_+&*-]+)*@" +
            "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
            "A-Z]{2,7}$");

    @Override
    public boolean isValidEmail(String email) {
        if (email == null)
            return false;
        return validEmailPattern.matcher(email).matches();
    }

    @Override
    public Uni<Void> sendEmail(String email, String content) {
        return Uni.createFrom().voidItem();
    }


}
