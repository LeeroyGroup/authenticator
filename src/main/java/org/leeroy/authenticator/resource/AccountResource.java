package org.leeroy.authenticator.resource;

import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpServerRequest;
import org.leeroy.authenticator.resource.request.*;
import org.leeroy.authenticator.service.AccountServiceBase;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/api/v1")
public class AccountResource {

    @Inject
    AccountServiceBase accountService;

    @Context
    private HttpServerRequest serverRequest;

    @POST
    @Path("account-authenticate")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> authenticate(AuthenticateRequest authenticateRequest) {
        return accountService.authenticate(getClientID(), authenticateRequest.username, authenticateRequest.password);
    }

    @PUT
    @Path("forgot-password")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> forgotPassword(ForgotPasswordRequest request) {
        return accountService.forgotPassword(getClientID(), request.username).onItem().transform(item -> "We sent you a email which you can use to set your password");
    }

    @POST
    @Path("create-account")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> createAccount(CreateAccountRequest request) {
        if (request.password != null) {
            return accountService.createAccount(getClientID(), request.username, request.password);
        } else {
            return accountService.createAccount(getClientID(), request.username);
        }
    }

    @PUT
    @Path("set-password")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> setPassword(SetPasswordRequest request) {
        return accountService.setPassword(getClientID(), request.token, request.newPassword).onItem().transform(item -> "Password changed");
    }

    @PUT
    @Path("change-password")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> changePassword(ChangePasswordRequest changePasswordRequest) {
        return accountService.changePassword(getClientID(), changePasswordRequest.username, changePasswordRequest.currentPassword, changePasswordRequest.newPassword).onItem().transform(item -> "Password changed");
    }

    @POST
    @Path("/delete-account")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> deleteAccount(DeleteAccountRequest request) {
        return accountService.deleteAccount(getClientID(), request.username, request.password).onItem().transform(item -> "Account deleted");
    }

    private ClientID getClientID() {
        ClientID clientID = new ClientID();
        clientID.ipAddress = serverRequest.remoteAddress().hostAddress();
        return clientID;
    }
}
