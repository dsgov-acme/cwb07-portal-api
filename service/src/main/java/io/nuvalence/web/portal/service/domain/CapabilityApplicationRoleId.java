package io.nuvalence.web.portal.service.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * Composite key for CapabilityApplicationRole.
 */
@Data
public class CapabilityApplicationRoleId implements Serializable {
    private static final long serialVersionUID = 1584222295673875129L;

    private UUID capability;
    private String applicationRole;
}
