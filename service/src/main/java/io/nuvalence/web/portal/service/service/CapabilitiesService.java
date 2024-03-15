package io.nuvalence.web.portal.service.service;

import io.nuvalence.auth.token.UserToken;
import io.nuvalence.events.event.RoleReportingEvent;
import io.nuvalence.events.exception.EventProcessingException;
import io.nuvalence.web.portal.service.domain.Capability;
import io.nuvalence.web.portal.service.domain.CapabilityApplicationRole;
import io.nuvalence.web.portal.service.repository.CapabilityApplicationRoleRepository;
import io.nuvalence.web.portal.service.repository.CapabilityRepository;
import io.nuvalence.web.portal.service.utils.auth.CurrentUserUtility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing capabilities.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CapabilitiesService {

    private final CapabilityApplicationRoleRepository capabilityAppRoleRepository;
    private final CapabilityRepository capabilityRepository;

    /**
     * Fetches the capabilities for the current user.
     *
     * @return List of capabilities.
     */
    public List<Capability> getCapabilitiesForCurrentUser() {
        List<String> roles =
                CurrentUserUtility.getCurrentUser().map(UserToken::getAllRoles).orElse(null);

        return capabilityAppRoleRepository.findAllByApplicationRoleIn(roles).stream()
                .map(CapabilityApplicationRole::getCapability)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Updates the mapping of capabilities to application roles based on the received event.
     *
     * @param event The event to process.
     * @throws EventProcessingException If roles are null
     */
    public void updateCapabilitiesFromRolesEvent(RoleReportingEvent event)
            throws EventProcessingException {
        var roles = event.getRoles();
        if (roles == null) {
            throw new EventProcessingException("Received roles are null");
        }
        for (var role : roles) {
            if (role.getCapabilities() == null || StringUtils.isBlank(role.getApplicationRole())) {
                if (StringUtils.isBlank(role.getApplicationRole())) {
                    log.error("Received a blank application role from " + event.getName());
                }
                continue;
            }

            List<Capability> storedCapabilities =
                    capabilityRepository.findAllByKeyIn(role.getCapabilities());

            List<Capability> newCapabilities =
                    role.getCapabilities().stream()
                            .filter(
                                    capability ->
                                            storedCapabilities.stream()
                                                    .noneMatch(
                                                            storedCapability ->
                                                                    storedCapability
                                                                            .getKey()
                                                                            .equals(capability)))
                            .map(capability -> Capability.builder().key(capability).build())
                            .toList();

            ArrayList<Capability> allCapabilities = new ArrayList<>();
            allCapabilities.addAll(storedCapabilities);
            allCapabilities.addAll(capabilityRepository.saveAll(newCapabilities));

            List<CapabilityApplicationRole> capabilityApplicationRoles =
                    allCapabilities.stream()
                            .map(
                                    capability ->
                                            CapabilityApplicationRole.builder()
                                                    .capability(capability)
                                                    .applicationRole(role.getApplicationRole())
                                                    .build())
                            .collect(Collectors.toList());

            capabilityAppRoleRepository.saveAll(capabilityApplicationRoles);
        }
    }
}
