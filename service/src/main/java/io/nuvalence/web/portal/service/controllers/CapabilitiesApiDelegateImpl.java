package io.nuvalence.web.portal.service.controllers;

import io.nuvalence.web.portal.service.domain.Capability;
import io.nuvalence.web.portal.service.generated.controllers.CapabilitiesApiDelegate;
import io.nuvalence.web.portal.service.service.CapabilitiesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the Capabilities API.
 */
@Service
@RequiredArgsConstructor
public class CapabilitiesApiDelegateImpl implements CapabilitiesApiDelegate {
    private final CapabilitiesService capabilitiesService;

    @Override
    public ResponseEntity<List<String>> getCapabilities() {
        List<String> capabilityKeys =
                capabilitiesService.getCapabilitiesForCurrentUser().stream()
                        .map(Capability::getKey)
                        .toList();
        return ResponseEntity.ok(capabilityKeys);
    }
}
