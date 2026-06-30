package com.difisoft.nhsv.admin.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.difisoft.nhsv.admin.domain.CopyPortfolioDetailHistory} entity. This class is used
 * in {@link com.difisoft.nhsv.admin.web.rest.CopyPortfolioDetailHistoryResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /copy-portfolio-detail-histories?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyPortfolioDetailHistoryCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter symbol;

    private DoubleFilter weight;

    private ZonedDateTimeFilter createdAt;

    private LongFilter copyPortfolioHistoryIdId;

    private LongFilter copyPortfolioHistoryId;

    private Boolean distinct;

    public CopyPortfolioDetailHistoryCriteria() {}

    public CopyPortfolioDetailHistoryCriteria(CopyPortfolioDetailHistoryCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.symbol = other.symbol == null ? null : other.symbol.copy();
        this.weight = other.weight == null ? null : other.weight.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.copyPortfolioHistoryIdId = other.copyPortfolioHistoryIdId == null ? null : other.copyPortfolioHistoryIdId.copy();
        this.copyPortfolioHistoryId = other.copyPortfolioHistoryId == null ? null : other.copyPortfolioHistoryId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CopyPortfolioDetailHistoryCriteria copy() {
        return new CopyPortfolioDetailHistoryCriteria(this);
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

    public StringFilter getSymbol() {
        return symbol;
    }

    public StringFilter symbol() {
        if (symbol == null) {
            symbol = new StringFilter();
        }
        return symbol;
    }

    public void setSymbol(StringFilter symbol) {
        this.symbol = symbol;
    }

    public DoubleFilter getWeight() {
        return weight;
    }

    public DoubleFilter weight() {
        if (weight == null) {
            weight = new DoubleFilter();
        }
        return weight;
    }

    public void setWeight(DoubleFilter weight) {
        this.weight = weight;
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

    public LongFilter getCopyPortfolioHistoryIdId() {
        return copyPortfolioHistoryIdId;
    }

    public LongFilter copyPortfolioHistoryIdId() {
        if (copyPortfolioHistoryIdId == null) {
            copyPortfolioHistoryIdId = new LongFilter();
        }
        return copyPortfolioHistoryIdId;
    }

    public void setCopyPortfolioHistoryIdId(LongFilter copyPortfolioHistoryIdId) {
        this.copyPortfolioHistoryIdId = copyPortfolioHistoryIdId;
    }

    public LongFilter getCopyPortfolioHistoryId() {
        return copyPortfolioHistoryId;
    }

    public LongFilter copyPortfolioHistoryId() {
        if (copyPortfolioHistoryId == null) {
            copyPortfolioHistoryId = new LongFilter();
        }
        return copyPortfolioHistoryId;
    }

    public void setCopyPortfolioHistoryId(LongFilter copyPortfolioHistoryId) {
        this.copyPortfolioHistoryId = copyPortfolioHistoryId;
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
        final CopyPortfolioDetailHistoryCriteria that = (CopyPortfolioDetailHistoryCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(symbol, that.symbol) &&
            Objects.equals(weight, that.weight) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(copyPortfolioHistoryIdId, that.copyPortfolioHistoryIdId) &&
            Objects.equals(copyPortfolioHistoryId, that.copyPortfolioHistoryId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, symbol, weight, createdAt, copyPortfolioHistoryIdId, copyPortfolioHistoryId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyPortfolioDetailHistoryCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (symbol != null ? "symbol=" + symbol + ", " : "") +
            (weight != null ? "weight=" + weight + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (copyPortfolioHistoryIdId != null ? "copyPortfolioHistoryIdId=" + copyPortfolioHistoryIdId + ", " : "") +
            (copyPortfolioHistoryId != null ? "copyPortfolioHistoryId=" + copyPortfolioHistoryId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
