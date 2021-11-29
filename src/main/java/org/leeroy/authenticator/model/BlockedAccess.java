package org.leeroy.authenticator.model;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import lombok.Builder;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonProperty;

import java.time.Instant;

@Data
@Builder
public class BlockedAccess extends PanacheMongoEntity {
    @BsonProperty("ip_address")
    public String ipAddress;
    public String device;
    public Instant timestamp;
    public String reason;
}
