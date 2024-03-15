package io.nuvalence.web.portal.service.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.nuvalence.web.portal.service.domain.Capability;
import io.nuvalence.web.portal.service.service.CapabilitiesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

class CapabilitiesApiDelegateImplTest {
    private CapabilitiesApiDelegateImpl delegate;

    private CapabilitiesService service;

    @BeforeEach
    public void setUp() {
        service = mock(CapabilitiesService.class);
        delegate = new CapabilitiesApiDelegateImpl(service);
    }

    @Test
    void getCapabilitiesTest() {
        List<Capability> capabilities =
                Arrays.asList(
                        Capability.builder().id(UUID.randomUUID()).key("capability1").build(),
                        Capability.builder().id(UUID.randomUUID()).key("capability2").build());
        when(service.getCapabilitiesForCurrentUser()).thenReturn(capabilities);

        var result = delegate.getCapabilities();

        assertEquals(200, result.getStatusCodeValue());
        assertTrue(result.getBody().contains("capability1"));
        assertTrue(result.getBody().contains("capability2"));
    }
}
