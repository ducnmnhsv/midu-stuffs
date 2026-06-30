package com.difisoft.nhsv.admin.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.difisoft.nhsv.admin.domain.CopyPortfolioHistory} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyPortfolioHistoryDTO implements Serializable {

    private Long id;

    @NotNull
    private ZonedDateTime createdAt;

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
        if (!(o instanceof CopyPortfolioHistoryDTO)) {
            return false;
        }

        CopyPortfolioHistoryDTO copyPortfolioHistoryDTO = (CopyPortfolioHistoryDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, copyPortfolioHistoryDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyPortfolioHistoryDTO{" +
            "id=" + getId() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", mlUserId=" + getMlUserId() +
            "}";
    }
}
