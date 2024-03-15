package io.nuvalence.web.portal.service.repository;

import io.nuvalence.web.portal.service.domain.Capability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for managing capabilities.
 */
public interface CapabilityRepository extends JpaRepository<Capability, UUID> {

    List<Capability> findAllByKeyIn(List<String> capabilityKeys);
}
