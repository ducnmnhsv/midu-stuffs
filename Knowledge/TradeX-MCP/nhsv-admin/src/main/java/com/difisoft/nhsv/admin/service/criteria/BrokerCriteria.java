package com.difisoft.nhsv.admin.service.criteria;

import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.difisoft.nhsv.admin.domain.Broker} entity. This class is used
 * in {@link com.difisoft.nhsv.admin.web.rest.BrokerResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /brokers?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class BrokerCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter username;

    private StringFilter fullname;

    private BooleanFilter status;

    private LongFilter totalChatRoom;

    private IntegerFilter currentRank;

    private BooleanFilter isDynamic;

    private StringFilter email;

    private ZonedDateTimeFilter createdAt;

    private ZonedDateTimeFilter updatedAt;

    private ZonedDateTimeFilter deactivatedAt;

    private StringFilter deactivatedBy;

    private StringFilter invitedBy;

    private Boolean distinct;

    public BrokerCriteria() {}

    public BrokerCriteria(BrokerCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.username = other.username == null ? null : other.username.copy();
        this.fullname = other.fullname == null ? null : other.fullname.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.totalChatRoom = other.totalChatRoom == null ? null : other.totalChatRoom.copy();
        this.currentRank = other.currentRank == null ? null : other.currentRank.copy();
        this.isDynamic = other.isDynamic == null ? null : other.isDynamic.copy();
        this.email = other.email == null ? null : other.email.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.updatedAt = other.updatedAt == null ? null : other.updatedAt.copy();
        this.deactivatedAt = other.deactivatedAt == null ? null : other.deactivatedAt.copy();
        this.deactivatedBy = other.deactivatedBy == null ? null : other.deactivatedBy.copy();
        this.invitedBy = other.invitedBy == null ? null : other.invitedBy.copy();
        this.distinct = other.distinct;
    }

    @Override
    public BrokerCriteria copy() {
        return new BrokerCriteria(this);
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

    public StringFilter getUsername() {
        return username;
    }

    public StringFilter username() {
        if (username == null) {
            username = new StringFilter();
        }
        return username;
    }

    public void setUsername(StringFilter username) {
        this.username = username;
    }

    public StringFilter getFullname() {
        return fullname;
    }

    public StringFilter fullname() {
        if (fullname == null) {
            fullname = new StringFilter();
        }
        return fullname;
    }

    public void setFullname(StringFilter fullname) {
        this.fullname = fullname;
    }

    public BooleanFilter getStatus() {
        return status;
    }

    public BooleanFilter status() {
        if (status == null) {
            status = new BooleanFilter();
        }
        return status;
    }

    public void setStatus(BooleanFilter status) {
        this.status = status;
    }

    public LongFilter getTotalChatRoom() {
        return totalChatRoom;
    }

    public LongFilter totalChatRoom() {
        if (totalChatRoom == null) {
            totalChatRoom = new LongFilter();
        }
        return totalChatRoom;
    }

    public void setTotalChatRoom(LongFilter totalChatRoom) {
        this.totalChatRoom = totalChatRoom;
    }

    public IntegerFilter getCurrentRank() {
        return currentRank;
    }

    public IntegerFilter currentRank() {
        if (currentRank == null) {
            currentRank = new IntegerFilter();
        }
        return currentRank;
    }

    public void setCurrentRank(IntegerFilter currentRank) {
        this.currentRank = currentRank;
    }

    public BooleanFilter getIsDynamic() {
        return isDynamic;
    }

    public BooleanFilter isDynamic() {
        if (isDynamic == null) {
            isDynamic = new BooleanFilter();
        }
        return isDynamic;
    }

    public void setIsDynamic(BooleanFilter isDynamic) {
        this.isDynamic = isDynamic;
    }

    public StringFilter getEmail() {
        return email;
    }

    public StringFilter email() {
        if (email == null) {
            email = new StringFilter();
        }
        return email;
    }

    public void setEmail(StringFilter email) {
        this.email = email;
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

    public ZonedDateTimeFilter getDeactivatedAt() {
        return deactivatedAt;
    }

    public ZonedDateTimeFilter deactivatedAt() {
        if (deactivatedAt == null) {
            deactivatedAt = new ZonedDateTimeFilter();
        }
        return deactivatedAt;
    }

    public void setDeactivatedAt(ZonedDateTimeFilter deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public StringFilter getDeactivatedBy() {
        return deactivatedBy;
    }

    public StringFilter deactivatedBy() {
        if (deactivatedBy == null) {
            deactivatedBy = new StringFilter();
        }
        return deactivatedBy;
    }

    public void setDeactivatedBy(StringFilter deactivatedBy) {
        this.deactivatedBy = deactivatedBy;
    }

    public StringFilter getInvitedBy() {
        return invitedBy;
    }

    public StringFilter invitedBy() {
        if (invitedBy == null) {
            invitedBy = new StringFilter();
        }
        return invitedBy;
    }

    public void setInvitedBy(StringFilter invitedBy) {
        this.invitedBy = invitedBy;
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
        final BrokerCriteria that = (BrokerCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(username, that.username) &&
            Objects.equals(fullname, that.fullname) &&
            Objects.equals(status, that.status) &&
            Objects.equals(totalChatRoom, that.totalChatRoom) &&
            Objects.equals(currentRank, that.currentRank) &&
            Objects.equals(isDynamic, that.isDynamic) &&
            Objects.equals(email, that.email) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(deactivatedAt, that.deactivatedAt) &&
            Objects.equals(deactivatedBy, that.deactivatedBy) &&
            Objects.equals(invitedBy, that.invitedBy) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            username,
            fullname,
            status,
            totalChatRoom,
            currentRank,
            isDynamic,
            email,
            createdAt,
            updatedAt,
            deactivatedAt,
            deactivatedBy,
            invitedBy,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "BrokerCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (username != null ? "username=" + username + ", " : "") +
            (fullname != null ? "fullname=" + fullname + ", " : "") +
            (status != null ? "status=" + status + ", " : "") +
            (totalChatRoom != null ? "totalChatRoom=" + totalChatRoom + ", " : "") +
            (currentRank != null ? "currentRank=" + currentRank + ", " : "") +
            (isDynamic != null ? "isDynamic=" + isDynamic + ", " : "") +
            (email != null ? "email=" + email + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (updatedAt != null ? "updatedAt=" + updatedAt + ", " : "") +
            (deactivatedAt != null ? "deactivatedAt=" + deactivatedAt + ", " : "") +
            (deactivatedBy != null ? "deactivatedBy=" + deactivatedBy + ", " : "") +
            (invitedBy != null ? "invitedBy=" + invitedBy + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
