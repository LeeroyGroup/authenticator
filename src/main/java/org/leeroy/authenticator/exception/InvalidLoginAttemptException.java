package org.leeroy.authenticator.exception;

public class InvalidLoginAttemptException extends Exception {

    public InvalidLoginAttemptException() {
        super("Invalid attempt login");
    }
}
