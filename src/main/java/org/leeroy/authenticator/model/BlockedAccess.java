package org.leeroy.authenticator.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import lombok.Builder;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.LocalDateTime;

@MongoEntity(collection = "blocked_ip")
@Data
@Builder
public class BlockedAccess {
    @BsonProperty("ip_address")
    private String ipAddress;
    private String device;
    private LocalDateTime timestamp;
    private String reason;
}
