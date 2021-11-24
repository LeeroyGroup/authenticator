package org.leeroy.authenticator.exception;

public class WaitBeforeTryingLoginAgainException extends Exception{

    public WaitBeforeTryingLoginAgainException(){
        super("You will have to wait a while before trying to login again");
    }
}
