package org.leeroy.authenticator.resource.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorMessage {

    private int code;
    private String message;
    private LocalDateTime timestamp;
}
