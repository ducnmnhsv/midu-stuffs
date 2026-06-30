package com.difisoft.nhsv.admin.service.criteria;

import com.difisoft.nhsv.admin.domain.enumeration.InviteStatusEnum;
import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.difisoft.nhsv.admin.domain.InviteUser} entity. This class is used
 * in {@link com.difisoft.nhsv.admin.web.rest.InviteUserResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /invite-users?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InviteUserCriteria implements Serializable, Criteria {

    /**
     * Class for filtering InviteStatusEnum
     */
    public static class InviteStatusEnumFilter extends Filter<InviteStatusEnum> {

        public InviteStatusEnumFilter() {}

        public InviteStatusEnumFilter(InviteStatusEnumFilter filter) {
            super(filter);
        }

        @Override
        public InviteStatusEnumFilter copy() {
            return new InviteStatusEnumFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter login;

    private StringFilter email;

    private InviteStatusEnumFilter status;

    private ZonedDateTimeFilter createdAt;

    private ZonedDateTimeFilter updatedAt;

    private LongFilter createdId;

    private StringFilter createdBy;

    private StringFilter activationKey;

    private ZonedDateTimeFilter activationDate;

    private StringFilter langKey;

    private StringFilter authorities;

    private Boolean distinct;

    public InviteUserCriteria() {}

    public InviteUserCriteria(InviteUserCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.login = other.login == null ? null : other.login.copy();
        this.email = other.email == null ? null : other.email.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.updatedAt = other.updatedAt == null ? null : other.updatedAt.copy();
        this.createdId = other.createdId == null ? null : other.createdId.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.activationKey = other.activationKey == null ? null : other.activationKey.copy();
        this.activationDate = other.activationDate == null ? null : other.activationDate.copy();
        this.langKey = other.langKey == null ? null : other.langKey.copy();
        this.authorities = other.authorities == null ? null : other.authorities.copy();
        this.distinct = other.distinct;
    }

    @Override
    public InviteUserCriteria copy() {
        return new InviteUserCriteria(this);
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

    public StringFilter getLogin() {
        return login;
    }

    public StringFilter login() {
        if (login == null) {
            login = new StringFilter();
        }
        return login;
    }

    public void setLogin(StringFilter login) {
        this.login = login;
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

    public InviteStatusEnumFilter getStatus() {
        return status;
    }

    public InviteStatusEnumFilter status() {
        if (status == null) {
            status = new InviteStatusEnumFilter();
        }
        return status;
    }

    public void setStatus(InviteStatusEnumFilter status) {
        this.status = status;
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

    public LongFilter getCreatedId() {
        return createdId;
    }

    public LongFilter createdId() {
        if (createdId == null) {
            createdId = new LongFilter();
        }
        return createdId;
    }

    public void setCreatedId(LongFilter createdId) {
        this.createdId = createdId;
    }

    public StringFilter getCreatedBy() {
        return createdBy;
    }

    public StringFilter createdBy() {
        if (createdBy == null) {
            createdBy = new StringFilter();
        }
        return createdBy;
    }

    public void setCreatedBy(StringFilter createdBy) {
        this.createdBy = createdBy;
    }

    public StringFilter getActivationKey() {
        return activationKey;
    }

    public StringFilter activationKey() {
        if (activationKey == null) {
            activationKey = new StringFilter();
        }
        return activationKey;
    }

    public void setActivationKey(StringFilter activationKey) {
        this.activationKey = activationKey;
    }

    public ZonedDateTimeFilter getActivationDate() {
        return activationDate;
    }

    public ZonedDateTimeFilter activationDate() {
        if (activationDate == null) {
            activationDate = new ZonedDateTimeFilter();
        }
        return activationDate;
    }

    public void setActivationDate(ZonedDateTimeFilter activationDate) {
        this.activationDate = activationDate;
    }

    public StringFilter getLangKey() {
        return langKey;
    }

    public StringFilter langKey() {
        if (langKey == null) {
            langKey = new StringFilter();
        }
        return langKey;
    }

    public void setLangKey(StringFilter langKey) {
        this.langKey = langKey;
    }

    public StringFilter getAuthorities() {
        return authorities;
    }

    public StringFilter authorities() {
        if (authorities == null) {
            authorities = new StringFilter();
        }
        return authorities;
    }

    public void setAuthorities(StringFilter authorities) {
        this.authorities = authorities;
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
        final InviteUserCriteria that = (InviteUserCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(login, that.login) &&
            Objects.equals(email, that.email) &&
            Objects.equals(status, that.status) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(createdId, that.createdId) &&
            Objects.equals(createdBy, that.createdBy) &&
            Objects.equals(activationKey, that.activationKey) &&
            Objects.equals(activationDate, that.activationDate) &&
            Objects.equals(langKey, that.langKey) &&
            Objects.equals(authorities, that.authorities) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            login,
            email,
            status,
            createdAt,
            updatedAt,
            createdId,
            createdBy,
            activationKey,
            activationDate,
            langKey,
            authorities,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InviteUserCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (login != null ? "login=" + login + ", " : "") +
            (email != null ? "email=" + email + ", " : "") +
            (status != null ? "status=" + status + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (updatedAt != null ? "updatedAt=" + updatedAt + ", " : "") +
            (createdId != null ? "createdId=" + createdId + ", " : "") +
            (createdBy != null ? "createdBy=" + createdBy + ", " : "") +
            (activationKey != null ? "activationKey=" + activationKey + ", " : "") +
            (activationDate != null ? "activationDate=" + activationDate + ", " : "") +
            (langKey != null ? "langKey=" + langKey + ", " : "") +
            (authorities != null ? "authorities=" + authorities + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
