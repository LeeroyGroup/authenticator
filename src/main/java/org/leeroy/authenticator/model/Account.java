package org.leeroy.authenticator.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Data;

@Data
public class Account extends PanacheMongoEntity {

    private Long id;
    private String username;
    private String password;
}
