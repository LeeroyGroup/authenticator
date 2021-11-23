package org.leeroy.authenticator.service;

public interface AccountService {

    void authenticate(String ipAddress, String device);
}
