package io.nuvalence.web.portal.service.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a capability that is available to a user.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "capability")
public class Capability {

    @Id
    @Column(name = "id", length = 36, updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "key", nullable = false, length = 1024)
    private String key;

    @Override
    public int hashCode() {
        return Objects.hash(id, key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Capability that = (Capability) o;
        return Objects.equals(id, that.id) && Objects.equals(key, that.key);
    }
}
