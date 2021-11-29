package org.leeroy.authenticator.resource.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticateRequest {

    private String ipAddress;
    private String device;
    private String channel;
    private String client;
    private String username;
    private String password;
}
