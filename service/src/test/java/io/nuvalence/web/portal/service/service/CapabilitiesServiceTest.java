package io.nuvalence.web.portal.service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.auth.token.UserToken;
import io.nuvalence.events.event.RoleReportingEvent;
import io.nuvalence.events.event.dto.ApplicationRole;
import io.nuvalence.events.exception.EventProcessingException;
import io.nuvalence.web.portal.service.domain.Capability;
import io.nuvalence.web.portal.service.domain.CapabilityApplicationRole;
import io.nuvalence.web.portal.service.repository.CapabilityApplicationRoleRepository;
import io.nuvalence.web.portal.service.repository.CapabilityRepository;
import io.nuvalence.web.portal.service.utils.auth.CurrentUserUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@ExtendWith(MockitoExtension.class)
class CapabilitiesServiceTest {

    private static final UUID preStoredNilUUID = new UUID(0, 0);

    private CapabilitiesService service;
    private CapabilityApplicationRoleRepository capabilityAppRoleRepository;
    private CapabilityRepository capabilityRepository;

    @BeforeEach
    public void setUp() {

        capabilityAppRoleRepository = mock(CapabilityApplicationRoleRepository.class);
        capabilityRepository = mock(CapabilityRepository.class);
        service = new CapabilitiesService(capabilityAppRoleRepository, capabilityRepository);
    }

    @Test
    void getCapabilitiesForCurrentUserDelegatesToCapabilityApplicationRoleRepository() {

        try (MockedStatic<CurrentUserUtility> mockedCurrentUserUtility =
                mockStatic(CurrentUserUtility.class)) {
            UserToken mockUserToken = mock(UserToken.class);
            when(mockUserToken.getAllRoles()).thenReturn(List.of("role1", "role2"));
            when(CurrentUserUtility.getCurrentUser()).thenReturn(Optional.of(mockUserToken));

            Capability capabilityOne =
                    Capability.builder().id(UUID.randomUUID()).key("capability1").build();

            Capability capabilityTwo =
                    Capability.builder().id(UUID.randomUUID()).key("capability2").build();

            CapabilityApplicationRole capabilityAppRoleOne =
                    CapabilityApplicationRole.builder()
                            .applicationRole("role1")
                            .capability(capabilityOne)
                            .build();

            CapabilityApplicationRole capabilityAppRoleTwo =
                    CapabilityApplicationRole.builder()
                            .applicationRole("role2")
                            .capability(capabilityTwo)
                            .build();

            CapabilityApplicationRole capabilityAppRoleThree =
                    CapabilityApplicationRole.builder()
                            .applicationRole("role2")
                            .capability(capabilityOne) // capabilityOne associated to both roles
                            .build();

            when(capabilityAppRoleRepository.findAllByApplicationRoleIn(List.of("role1", "role2")))
                    .thenReturn(
                            List.of(
                                    capabilityAppRoleOne,
                                    capabilityAppRoleTwo,
                                    capabilityAppRoleThree));

            List<Capability> result = service.getCapabilitiesForCurrentUser();

            assertEquals(2, result.size()); // should return only unique capabilities
            assertTrue(result.contains(capabilityAppRoleOne.getCapability()));
            assertTrue(result.contains(capabilityAppRoleTwo.getCapability()));
        }
    }

    @Test
    void updateCapabilities_NullRoles() {

        RoleReportingEvent event = new RoleReportingEvent();
        event.setRoles(null);

        var e =
                assertThrows(
                        EventProcessingException.class,
                        () -> service.updateCapabilitiesFromRolesEvent(event));

        assertTrue(e.getMessage().contains("Received roles are null"));
    }

    @Test
    @ExtendWith(OutputCaptureExtension.class)
    void updateCapabilities_NoActions(CapturedOutput output) throws EventProcessingException {

        ApplicationRole appRole1 =
                ApplicationRole.builder().applicationRole(" ").capabilities(List.of()).build();

        ApplicationRole appRole2 =
                ApplicationRole.builder().applicationRole("no-capabilities").build();

        ApplicationRole appRole3 =
                ApplicationRole.builder()
                        .applicationRole("empty-capabilities")
                        .capabilities(List.of())
                        .build();

        RoleReportingEvent event = new RoleReportingEvent();
        event.setName("test");
        event.setRoles(List.of(appRole1, appRole2, appRole3));

        service.updateCapabilitiesFromRolesEvent(event);

        assertTrue(
                output.getOut()
                        .lines()
                        .anyMatch(
                                line ->
                                        line.contains("ERROR")
                                                && line.contains(
                                                        "Received a blank application role from"
                                                                + " test")));

        verify(capabilityRepository, times(1)).findAllByKeyIn(List.of());

        verify(capabilityRepository, times(1)).saveAll(List.of());

        verify(capabilityAppRoleRepository, times(1)).saveAll(List.of());

        verifyNoMoreInteractions(capabilityRepository, capabilityAppRoleRepository);
    }

    @Test
    void updateCapabilities_AllNewCapabilities() throws Exception {

        stubCapabilitiesSaveAll();

        RoleReportingEvent event = getTestingRolesEvent();

        service.updateCapabilitiesFromRolesEvent(event);

        // verify saved capabilities
        ArgumentCaptor<List<Capability>> capabilitiesSaverCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(capabilityRepository, times(2)).saveAll(capabilitiesSaverCaptor.capture());

        List<List<Capability>> allCapturedCaps = capabilitiesSaverCaptor.getAllValues();
        List<Capability> firstSavedCaps = allCapturedCaps.get(0);

        assertEquals(2, firstSavedCaps.size());
        assertEquals("transaction-management-read", firstSavedCaps.get(0).getKey());
        assertEquals("transaction-management-write", firstSavedCaps.get(1).getKey());

        List<Capability> secondSavedCaps = allCapturedCaps.get(1);
        assertEquals(4, secondSavedCaps.size());
        assertEquals("admin-console", secondSavedCaps.get(0).getKey());
        assertEquals("transaction-config", secondSavedCaps.get(1).getKey());
        assertEquals("transaction-set-config", secondSavedCaps.get(2).getKey());
        assertEquals("schema-config", secondSavedCaps.get(3).getKey());

        // verify saved app roles mappings
        ArgumentCaptor<List<CapabilityApplicationRole>> capabilityAppRoleSaverCaptor =
                ArgumentCaptor.forClass(List.class);

        verify(capabilityAppRoleRepository, times(2))
                .saveAll(capabilityAppRoleSaverCaptor.capture());

        List<List<CapabilityApplicationRole>> allCapturedAppRoles =
                capabilityAppRoleSaverCaptor.getAllValues();
        List<CapabilityApplicationRole> firstSavedAppRoles = allCapturedAppRoles.get(0);

        List<CapabilityApplicationRole> secondSavedAppRoles = allCapturedAppRoles.get(1);
        assertEquals(4, secondSavedAppRoles.size());
        assertEquals(
                "wm:transaction-config-admin", secondSavedAppRoles.get(0).getApplicationRole());
        assertEquals("admin-console", secondSavedAppRoles.get(0).getCapability().getKey());
        assertEquals(
                "wm:transaction-config-admin", secondSavedAppRoles.get(1).getApplicationRole());
        assertEquals("transaction-config", secondSavedAppRoles.get(1).getCapability().getKey());
        assertEquals(
                "wm:transaction-config-admin", secondSavedAppRoles.get(2).getApplicationRole());
        assertEquals("transaction-set-config", secondSavedAppRoles.get(2).getCapability().getKey());
        assertEquals(
                "wm:transaction-config-admin", secondSavedAppRoles.get(3).getApplicationRole());
        assertEquals("schema-config", secondSavedAppRoles.get(3).getCapability().getKey());
    }

    @Test
    void updateCapabilities_SomeNewCapabilities() throws Exception {

        stubCapabilitiesSaveAll();

        RoleReportingEvent event = getTestingRolesEvent();

        when(capabilityRepository.findAllByKeyIn(
                        List.of("transaction-management-read", "transaction-management-write")))
                .thenReturn(
                        List.of(
                                Capability.builder()
                                        .key("transaction-management-read")
                                        .id(preStoredNilUUID)
                                        .build()));

        when(capabilityRepository.findAllByKeyIn(
                        List.of(
                                "admin-console",
                                "transaction-config",
                                "transaction-set-config",
                                "schema-config")))
                .thenReturn(
                        List.of(
                                Capability.builder()
                                        .key("admin-console")
                                        .id(preStoredNilUUID)
                                        .build(),
                                Capability.builder()
                                        .id(preStoredNilUUID)
                                        .key("transaction-set-config")
                                        .build()));

        service.updateCapabilitiesFromRolesEvent(event);

        verifySavedCapabilities();

        verifySavedAppRoles();
    }

    private void stubCapabilitiesSaveAll() {
        when(capabilityRepository.saveAll(anyList()))
                .thenAnswer(
                        invocation -> {
                            List<Capability> capabilities = invocation.getArgument(0);
                            List<Capability> savedCapabilities =
                                    capabilities.stream()
                                            .map(
                                                    capability ->
                                                            Capability.builder()
                                                                    .id(UUID.randomUUID())
                                                                    .key(capability.getKey())
                                                                    .build())
                                            .collect(Collectors.toList());
                            return savedCapabilities;
                        });
    }

    private RoleReportingEvent getTestingRolesEvent() throws Exception {

        String appRoles =
                """
        {
                "name": "work-manager",
                "roles": [
                        {
                        "applicationRole": "wm:transaction-submitter",
                        "name": "Transaction Submitter",
                        "description": "Can create transactions. Can view & update transactions they submitted.",
                        "group": "work-manager"
                        },
                        {
                        "applicationRole": "wm:transaction-admin",
                        "name": "Transaction Admin",
                        "description": "Can create transactions. Can create & update all transactions.",
                        "group": "work-manager",
                                "capabilities": [
                                "transaction-management-read",
                                "transaction-management-write"
                                ]
                        },
                        {
                        "applicationRole": "wm:transaction-config-admin",
                        "name": "Transaction Configuration Admin",
                        "description": "Can create & update all definitions and configurations.",
                        "group": "work-manager",
                                "capabilities": [
                                "admin-console",
                                "transaction-config",
                                "transaction-set-config",
                                "schema-config"
                                ]
                        }
                ]
        }
                """;

        return new ObjectMapper().readValue(appRoles, RoleReportingEvent.class);
    }

    private void verifySavedCapabilities() {
        ArgumentCaptor<List<Capability>> capabilitiesSaverCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(capabilityRepository, times(2)).saveAll(capabilitiesSaverCaptor.capture());

        List<List<Capability>> allCapturedCaps = capabilitiesSaverCaptor.getAllValues();

        // Assertions for the first set of saved capabilities
        List<Capability> firstSavedCaps = allCapturedCaps.get(0);
        assertEquals(1, firstSavedCaps.size());
        assertEquals("transaction-management-write", firstSavedCaps.get(0).getKey());

        // Assertions for the second set of saved capabilities
        List<Capability> secondSavedCaps = allCapturedCaps.get(1);
        assertEquals(2, secondSavedCaps.size());
        assertEquals("transaction-config", secondSavedCaps.get(0).getKey());
        assertEquals("schema-config", secondSavedCaps.get(1).getKey());
    }

    private void verifySavedAppRoles() {
        ArgumentCaptor<List<CapabilityApplicationRole>> capabilityAppRoleSaverCaptor =
                ArgumentCaptor.forClass(List.class);
        verify(capabilityAppRoleRepository, times(2))
                .saveAll(capabilityAppRoleSaverCaptor.capture());

        List<List<CapabilityApplicationRole>> allCapturedAppRoles =
                capabilityAppRoleSaverCaptor.getAllValues();

        // Assertions for the first set of saved app roles
        List<CapabilityApplicationRole> firstSavedAppRoles = allCapturedAppRoles.get(0);
        assertEquals(2, firstSavedAppRoles.size());
        assertEquals("wm:transaction-admin", firstSavedAppRoles.get(0).getApplicationRole());
        assertEquals(
                "transaction-management-read", firstSavedAppRoles.get(0).getCapability().getKey());
        assertEquals(preStoredNilUUID, firstSavedAppRoles.get(0).getCapability().getId());

        assertEquals("wm:transaction-admin", firstSavedAppRoles.get(1).getApplicationRole());
        assertEquals(
                "transaction-management-write", firstSavedAppRoles.get(1).getCapability().getKey());
        assertNotEquals(preStoredNilUUID, firstSavedAppRoles.get(1).getCapability().getId());

        // Assertions for the second set of saved app roles
        List<CapabilityApplicationRole> secondSavedAppRoles = allCapturedAppRoles.get(1);
        assertEquals(4, secondSavedAppRoles.size());
        assertEquals(
                "wm:transaction-config-admin", secondSavedAppRoles.get(0).getApplicationRole());
        assertEquals("admin-console", secondSavedAppRoles.get(0).getCapability().getKey());
        assertEquals(preStoredNilUUID, secondSavedAppRoles.get(0).getCapability().getId());
        assertEquals(
                "wm:transaction-config-admin", secondSavedAppRoles.get(1).getApplicationRole());
        assertEquals("transaction-set-config", secondSavedAppRoles.get(1).getCapability().getKey());
        assertEquals(preStoredNilUUID, secondSavedAppRoles.get(1).getCapability().getId());
        assertEquals(
                "wm:transaction-config-admin", secondSavedAppRoles.get(2).getApplicationRole());
        assertEquals("transaction-config", secondSavedAppRoles.get(2).getCapability().getKey());
        assertNotEquals(preStoredNilUUID, secondSavedAppRoles.get(2).getCapability().getId());
        assertEquals(
                "wm:transaction-config-admin", secondSavedAppRoles.get(3).getApplicationRole());
        assertEquals("schema-config", secondSavedAppRoles.get(3).getCapability().getKey());
        assertNotEquals(preStoredNilUUID, secondSavedAppRoles.get(3).getCapability().getId());
    }
}
