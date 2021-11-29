package org.leeroy.authenticator.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Account extends PanacheMongoEntity {
    public String username;
    public String password;
}
