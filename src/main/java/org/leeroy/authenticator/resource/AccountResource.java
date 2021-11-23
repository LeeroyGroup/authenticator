package org.leeroy.authenticator.resource;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/v1")
public class AccountResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/authenticate")
    public Response authenticate(){
        return Response.ok().build();
    }
}
