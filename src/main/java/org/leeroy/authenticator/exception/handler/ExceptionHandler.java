package org.leeroy.authenticator.exception.handler;

import org.leeroy.authenticator.resource.response.ErrorMessage;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.time.LocalDateTime;

public class ExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        // TODO We can validate the instanceof the exception
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(ErrorMessage.builder()
                        .code(-1)
                        .message(exception.getMessage())
                        .timestamp(LocalDateTime.now()).build())
                .build();
    }
}
