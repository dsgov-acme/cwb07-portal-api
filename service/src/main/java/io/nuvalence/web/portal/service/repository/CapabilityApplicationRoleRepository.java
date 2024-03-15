package io.nuvalence.web.portal.service.repository;

import io.nuvalence.web.portal.service.domain.CapabilityApplicationRole;
import io.nuvalence.web.portal.service.domain.CapabilityApplicationRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for CapabilityApplicationRole.
 */
public interface CapabilityApplicationRoleRepository
        extends JpaRepository<CapabilityApplicationRole, CapabilityApplicationRoleId> {

    List<CapabilityApplicationRole> findAllByApplicationRoleIn(List<String> roles);
}
