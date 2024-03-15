package io.nuvalence.web.portal.service.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import java.util.UUID;

class CapabilityTest {

    private final UUID id = UUID.randomUUID();
    private final String key = "key";
    private final Capability capability1 = Capability.builder().id(id).key(key).build();
    private final Capability capability2 = Capability.builder().id(id).key(key).build();

    @Test
    void testEqualsTrue() {
        assertEquals(capability1, capability2);
    }

    @Test
    void testEqualsFalse() {
        Capability capability3 = Capability.builder().id(UUID.randomUUID()).key(key).build();
        assertNotEquals(capability1, capability3);
        assertNotEquals("key", capability1);
    }
}
