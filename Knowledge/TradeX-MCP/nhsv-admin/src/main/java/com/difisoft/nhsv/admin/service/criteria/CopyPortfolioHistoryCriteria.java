package com.difisoft.nhsv.admin.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.difisoft.nhsv.admin.domain.CopyPortfolioHistory} entity. This class is used
 * in {@link com.difisoft.nhsv.admin.web.rest.CopyPortfolioHistoryResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /copy-portfolio-histories?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyPortfolioHistoryCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private ZonedDateTimeFilter createdAt;

    private LongFilter copyPortfolioDetailHistoryId;

    private LongFilter mlUserIdId;

    private Boolean distinct;

    public CopyPortfolioHistoryCriteria() {}

    public CopyPortfolioHistoryCriteria(CopyPortfolioHistoryCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.copyPortfolioDetailHistoryId = other.copyPortfolioDetailHistoryId == null ? null : other.copyPortfolioDetailHistoryId.copy();
        this.mlUserIdId = other.mlUserIdId == null ? null : other.mlUserIdId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CopyPortfolioHistoryCriteria copy() {
        return new CopyPortfolioHistoryCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public LongFilter id() {
        if (id == null) {
            id = new LongFilter();
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public ZonedDateTimeFilter getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTimeFilter createdAt() {
        if (createdAt == null) {
            createdAt = new ZonedDateTimeFilter();
        }
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTimeFilter createdAt) {
        this.createdAt = createdAt;
    }

    public LongFilter getCopyPortfolioDetailHistoryId() {
        return copyPortfolioDetailHistoryId;
    }

    public LongFilter copyPortfolioDetailHistoryId() {
        if (copyPortfolioDetailHistoryId == null) {
            copyPortfolioDetailHistoryId = new LongFilter();
        }
        return copyPortfolioDetailHistoryId;
    }

    public void setCopyPortfolioDetailHistoryId(LongFilter copyPortfolioDetailHistoryId) {
        this.copyPortfolioDetailHistoryId = copyPortfolioDetailHistoryId;
    }

    public LongFilter getMlUserIdId() {
        return mlUserIdId;
    }

    public LongFilter mlUserIdId() {
        if (mlUserIdId == null) {
            mlUserIdId = new LongFilter();
        }
        return mlUserIdId;
    }

    public void setMlUserIdId(LongFilter mlUserIdId) {
        this.mlUserIdId = mlUserIdId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CopyPortfolioHistoryCriteria that = (CopyPortfolioHistoryCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(copyPortfolioDetailHistoryId, that.copyPortfolioDetailHistoryId) &&
            Objects.equals(mlUserIdId, that.mlUserIdId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, copyPortfolioDetailHistoryId, mlUserIdId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyPortfolioHistoryCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (copyPortfolioDetailHistoryId != null ? "copyPortfolioDetailHistoryId=" + copyPortfolioDetailHistoryId + ", " : "") +
            (mlUserIdId != null ? "mlUserIdId=" + mlUserIdId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
