package com.difisoft.nhsv.admin.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.difisoft.nhsv.admin.domain.CopyMarketLeaderDetails} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyMarketLeaderDetailsDTO implements Serializable {

    private Long id;

    @NotNull
    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    @NotNull
    @Size(max = 255)
    private String type;

    @NotNull
    @Size(max = 255)
    private String label;

    @NotNull
    @Size(max = 255)
    private String key;

    @NotNull
    @Size(max = 2000)
    private String value;

    private UserDTO mlUserId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public UserDTO getMlUserId() {
        return mlUserId;
    }

    public void setMlUserId(UserDTO mlUserId) {
        this.mlUserId = mlUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CopyMarketLeaderDetailsDTO)) {
            return false;
        }

        CopyMarketLeaderDetailsDTO copyMarketLeaderDetailsDTO = (CopyMarketLeaderDetailsDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, copyMarketLeaderDetailsDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyMarketLeaderDetailsDTO{" +
            "id=" + getId() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", type='" + getType() + "'" +
            ", label='" + getLabel() + "'" +
            ", key='" + getKey() + "'" +
            ", value='" + getValue() + "'" +
            ", mlUserId=" + getMlUserId() +
            "}";
    }
}
