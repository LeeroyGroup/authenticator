package org.leeroy.authenticator.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Account extends PanacheMongoEntity {
    private String id;
    private String username;
    private String password;
}
