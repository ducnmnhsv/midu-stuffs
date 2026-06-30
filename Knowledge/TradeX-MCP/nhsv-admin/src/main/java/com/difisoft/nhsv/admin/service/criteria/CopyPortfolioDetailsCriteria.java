package com.difisoft.nhsv.admin.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.difisoft.nhsv.admin.domain.CopyPortfolioDetails} entity. This class is used
 * in {@link com.difisoft.nhsv.admin.web.rest.CopyPortfolioDetailsResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /copy-portfolio-details?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyPortfolioDetailsCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter symbol;

    private DoubleFilter weight;

    private ZonedDateTimeFilter createdAt;

    private LongFilter copyPortfolioIdId;

    private LongFilter copyPortfolioId;

    private Boolean distinct;

    public CopyPortfolioDetailsCriteria() {}

    public CopyPortfolioDetailsCriteria(CopyPortfolioDetailsCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.symbol = other.symbol == null ? null : other.symbol.copy();
        this.weight = other.weight == null ? null : other.weight.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.copyPortfolioIdId = other.copyPortfolioIdId == null ? null : other.copyPortfolioIdId.copy();
        this.copyPortfolioId = other.copyPortfolioId == null ? null : other.copyPortfolioId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CopyPortfolioDetailsCriteria copy() {
        return new CopyPortfolioDetailsCriteria(this);
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

    public LongFilter getCopyPortfolioIdId() {
        return copyPortfolioIdId;
    }

    public LongFilter copyPortfolioIdId() {
        if (copyPortfolioIdId == null) {
            copyPortfolioIdId = new LongFilter();
        }
        return copyPortfolioIdId;
    }

    public void setCopyPortfolioIdId(LongFilter copyPortfolioIdId) {
        this.copyPortfolioIdId = copyPortfolioIdId;
    }

    public LongFilter getCopyPortfolioId() {
        return copyPortfolioId;
    }

    public LongFilter copyPortfolioId() {
        if (copyPortfolioId == null) {
            copyPortfolioId = new LongFilter();
        }
        return copyPortfolioId;
    }

    public void setCopyPortfolioId(LongFilter copyPortfolioId) {
        this.copyPortfolioId = copyPortfolioId;
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
        final CopyPortfolioDetailsCriteria that = (CopyPortfolioDetailsCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(symbol, that.symbol) &&
            Objects.equals(weight, that.weight) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(copyPortfolioIdId, that.copyPortfolioIdId) &&
            Objects.equals(copyPortfolioId, that.copyPortfolioId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, symbol, weight, createdAt, copyPortfolioIdId, copyPortfolioId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyPortfolioDetailsCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (symbol != null ? "symbol=" + symbol + ", " : "") +
            (weight != null ? "weight=" + weight + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (copyPortfolioIdId != null ? "copyPortfolioIdId=" + copyPortfolioIdId + ", " : "") +
            (copyPortfolioId != null ? "copyPortfolioId=" + copyPortfolioId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
