package org.leeroy.authenticator.resource;

import org.leeroy.authenticator.exception.InvalidLoginAttemptException;
import org.leeroy.authenticator.exception.WaitBeforeTryingLoginAgainException;
import org.leeroy.authenticator.resource.request.AuthenticateRequest;
import org.leeroy.authenticator.service.AccountService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/v1")
public class AccountResource {

    @Inject
    AccountService accountService;

    /**
     * @param authenticateRequest
     * @return
     * @throws InvalidLoginAttemptException
     * @throws WaitBeforeTryingLoginAgainException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/authenticate")
    public Response authenticate(AuthenticateRequest authenticateRequest) throws InvalidLoginAttemptException,
            WaitBeforeTryingLoginAgainException {
        accountService.authenticate(authenticateRequest);
        return Response.ok().build();
    }
}
