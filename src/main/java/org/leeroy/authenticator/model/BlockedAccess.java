package org.leeroy.authenticator.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Builder;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.LocalDateTime;

@Data
@Builder
public class BlockedAccess extends PanacheMongoEntity {
    @BsonProperty("ip_address")
    private String ipAddress;
    private String device;
    private LocalDateTime timestamp;
    private String reason;
}
