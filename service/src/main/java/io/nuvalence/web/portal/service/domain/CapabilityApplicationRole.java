package io.nuvalence.web.portal.service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents a capability that is available to a user with a given application role.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "capability_application_role")
@IdClass(CapabilityApplicationRoleId.class)
public class CapabilityApplicationRole {
    @Id
    @ManyToOne
    @JoinColumn(name = "capability_id", referencedColumnName = "id", nullable = false)
    private Capability capability;

    @Id
    @Column(name = "application_role", nullable = false, length = 1024)
    private String applicationRole;
}
