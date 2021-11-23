package org.leeroy.authenticator.resource;

import org.leeroy.authenticator.repository.AccountRepository;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/v1")
public class AccountResource {

    @Inject
    AccountRepository accountRepository;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/authenticate")
    public Response authenticate(){
        return Response.ok().build();
    }
}
