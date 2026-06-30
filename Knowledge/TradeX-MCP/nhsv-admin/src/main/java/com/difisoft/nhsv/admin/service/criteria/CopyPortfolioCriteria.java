package com.difisoft.nhsv.admin.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.difisoft.nhsv.admin.domain.CopyPortfolio} entity. This class is used
 * in {@link com.difisoft.nhsv.admin.web.rest.CopyPortfolioResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /copy-portfolios?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyPortfolioCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private ZonedDateTimeFilter createdAt;

    private LongFilter copyPortfolioDetailsId;

    private LongFilter mlUserIdId;

    private Boolean distinct;

    public CopyPortfolioCriteria() {}

    public CopyPortfolioCriteria(CopyPortfolioCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.copyPortfolioDetailsId = other.copyPortfolioDetailsId == null ? null : other.copyPortfolioDetailsId.copy();
        this.mlUserIdId = other.mlUserIdId == null ? null : other.mlUserIdId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CopyPortfolioCriteria copy() {
        return new CopyPortfolioCriteria(this);
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

    public LongFilter getCopyPortfolioDetailsId() {
        return copyPortfolioDetailsId;
    }

    public LongFilter copyPortfolioDetailsId() {
        if (copyPortfolioDetailsId == null) {
            copyPortfolioDetailsId = new LongFilter();
        }
        return copyPortfolioDetailsId;
    }

    public void setCopyPortfolioDetailsId(LongFilter copyPortfolioDetailsId) {
        this.copyPortfolioDetailsId = copyPortfolioDetailsId;
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
        final CopyPortfolioCriteria that = (CopyPortfolioCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(copyPortfolioDetailsId, that.copyPortfolioDetailsId) &&
            Objects.equals(mlUserIdId, that.mlUserIdId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, copyPortfolioDetailsId, mlUserIdId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyPortfolioCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (copyPortfolioDetailsId != null ? "copyPortfolioDetailsId=" + copyPortfolioDetailsId + ", " : "") +
            (mlUserIdId != null ? "mlUserIdId=" + mlUserIdId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
