package org.leeroy.authenticator.service;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@QuarkusTest
public class BlockedAccessServiceTest {

    @InjectMock
    BlockedAccessService blockedAccessService;

    @Test
    public void isBlocked() {

        Mockito.when(blockedAccessService.isBlocked("127.0.0.1", "android"))
                .thenReturn(Uni.createFrom().item(false));

        Assertions.assertEquals(
                Uni.createFrom().item(false).await().indefinitely(),
                blockedAccessService.isBlocked("127.0.0.1", "android").await().indefinitely());

    }
}
