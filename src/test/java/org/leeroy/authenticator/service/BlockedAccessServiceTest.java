package org.leeroy.authenticator.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class BlockedAccessServiceTest {

    @Inject
    BlockedAccessService blockedAccessService;

    @Test
    public void isBlocked() {

    }
}
