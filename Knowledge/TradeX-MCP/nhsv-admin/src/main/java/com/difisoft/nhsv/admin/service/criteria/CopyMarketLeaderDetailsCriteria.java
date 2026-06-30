package com.difisoft.nhsv.admin.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.difisoft.nhsv.admin.domain.CopyMarketLeaderDetails} entity. This class is used
 * in {@link com.difisoft.nhsv.admin.web.rest.CopyMarketLeaderDetailsResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /copy-market-leader-details?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CopyMarketLeaderDetailsCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private ZonedDateTimeFilter createdAt;

    private ZonedDateTimeFilter updatedAt;

    private StringFilter type;

    private StringFilter label;

    private StringFilter key;

    private StringFilter value;

    private LongFilter mlUserIdId;

    private Boolean distinct;

    public CopyMarketLeaderDetailsCriteria() {}

    public CopyMarketLeaderDetailsCriteria(CopyMarketLeaderDetailsCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.updatedAt = other.updatedAt == null ? null : other.updatedAt.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.label = other.label == null ? null : other.label.copy();
        this.key = other.key == null ? null : other.key.copy();
        this.value = other.value == null ? null : other.value.copy();
        this.mlUserIdId = other.mlUserIdId == null ? null : other.mlUserIdId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public CopyMarketLeaderDetailsCriteria copy() {
        return new CopyMarketLeaderDetailsCriteria(this);
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

    public ZonedDateTimeFilter getUpdatedAt() {
        return updatedAt;
    }

    public ZonedDateTimeFilter updatedAt() {
        if (updatedAt == null) {
            updatedAt = new ZonedDateTimeFilter();
        }
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTimeFilter updatedAt) {
        this.updatedAt = updatedAt;
    }

    public StringFilter getType() {
        return type;
    }

    public StringFilter type() {
        if (type == null) {
            type = new StringFilter();
        }
        return type;
    }

    public void setType(StringFilter type) {
        this.type = type;
    }

    public StringFilter getLabel() {
        return label;
    }

    public StringFilter label() {
        if (label == null) {
            label = new StringFilter();
        }
        return label;
    }

    public void setLabel(StringFilter label) {
        this.label = label;
    }

    public StringFilter getKey() {
        return key;
    }

    public StringFilter key() {
        if (key == null) {
            key = new StringFilter();
        }
        return key;
    }

    public void setKey(StringFilter key) {
        this.key = key;
    }

    public StringFilter getValue() {
        return value;
    }

    public StringFilter value() {
        if (value == null) {
            value = new StringFilter();
        }
        return value;
    }

    public void setValue(StringFilter value) {
        this.value = value;
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
        final CopyMarketLeaderDetailsCriteria that = (CopyMarketLeaderDetailsCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(type, that.type) &&
            Objects.equals(label, that.label) &&
            Objects.equals(key, that.key) &&
            Objects.equals(value, that.value) &&
            Objects.equals(mlUserIdId, that.mlUserIdId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt, updatedAt, type, label, key, value, mlUserIdId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CopyMarketLeaderDetailsCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (updatedAt != null ? "updatedAt=" + updatedAt + ", " : "") +
            (type != null ? "type=" + type + ", " : "") +
            (label != null ? "label=" + label + ", " : "") +
            (key != null ? "key=" + key + ", " : "") +
            (value != null ? "value=" + value + ", " : "") +
            (mlUserIdId != null ? "mlUserIdId=" + mlUserIdId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
