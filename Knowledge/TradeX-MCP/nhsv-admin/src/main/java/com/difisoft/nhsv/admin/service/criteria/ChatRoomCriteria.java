package com.difisoft.nhsv.admin.service.criteria;

import com.difisoft.nhsv.admin.domain.enumeration.ActionEnum;
import com.difisoft.nhsv.admin.domain.enumeration.StatusEnum;
import java.io.Serializable;
import java.util.Objects;
import org.springdoc.api.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link com.difisoft.nhsv.admin.domain.ChatRoom} entity. This class is used
 * in {@link com.difisoft.nhsv.admin.web.rest.ChatRoomResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /chat-rooms?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ChatRoomCriteria implements Serializable, Criteria {

    /**
     * Class for filtering StatusEnum
     */
    public static class StatusEnumFilter extends Filter<StatusEnum> {

        public StatusEnumFilter() {}

        public StatusEnumFilter(StatusEnumFilter filter) {
            super(filter);
        }

        @Override
        public StatusEnumFilter copy() {
            return new StatusEnumFilter(this);
        }
    }

    /**
     * Class for filtering ActionEnum
     */
    public static class ActionEnumFilter extends Filter<ActionEnum> {

        public ActionEnumFilter() {}

        public ActionEnumFilter(ActionEnumFilter filter) {
            super(filter);
        }

        @Override
        public ActionEnumFilter copy() {
            return new ActionEnumFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter groupName;

    private StringFilter groupOwner;

    private StringFilter introduction;

    private StringFilter photo;

    private StringFilter brokerName;

    private StringFilter brokerContact;

    private StatusEnumFilter status;

    private StringFilter createdBy;

    private ZonedDateTimeFilter createdAt;

    private ZonedDateTimeFilter updatedAt;

    private ZonedDateTimeFilter approvedAt;

    private ZonedDateTimeFilter rejectedAt;

    private StringFilter rejectReason;

    private StringFilter approvedBy;

    private StringFilter rejectedBy;

    private ActionEnumFilter action;

    private LongFilter socialLinkId;

    private Boolean distinct;

    public ChatRoomCriteria() {}

    public ChatRoomCriteria(ChatRoomCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.groupName = other.groupName == null ? null : other.groupName.copy();
        this.groupOwner = other.groupOwner == null ? null : other.groupOwner.copy();
        this.introduction = other.introduction == null ? null : other.introduction.copy();
        this.photo = other.photo == null ? null : other.photo.copy();
        this.brokerName = other.brokerName == null ? null : other.brokerName.copy();
        this.brokerContact = other.brokerContact == null ? null : other.brokerContact.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.createdAt = other.createdAt == null ? null : other.createdAt.copy();
        this.updatedAt = other.updatedAt == null ? null : other.updatedAt.copy();
        this.approvedAt = other.approvedAt == null ? null : other.approvedAt.copy();
        this.rejectedAt = other.rejectedAt == null ? null : other.rejectedAt.copy();
        this.rejectReason = other.rejectReason == null ? null : other.rejectReason.copy();
        this.approvedBy = other.approvedBy == null ? null : other.approvedBy.copy();
        this.rejectedBy = other.rejectedBy == null ? null : other.rejectedBy.copy();
        this.action = other.action == null ? null : other.action.copy();
        this.socialLinkId = other.socialLinkId == null ? null : other.socialLinkId.copy();
        this.distinct = other.distinct;
    }

    @Override
    public ChatRoomCriteria copy() {
        return new ChatRoomCriteria(this);
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

    public StringFilter getGroupName() {
        return groupName;
    }

    public StringFilter groupName() {
        if (groupName == null) {
            groupName = new StringFilter();
        }
        return groupName;
    }

    public void setGroupName(StringFilter groupName) {
        this.groupName = groupName;
    }

    public StringFilter getGroupOwner() {
        return groupOwner;
    }

    public StringFilter groupOwner() {
        if (groupOwner == null) {
            groupOwner = new StringFilter();
        }
        return groupOwner;
    }

    public void setGroupOwner(StringFilter groupOwner) {
        this.groupOwner = groupOwner;
    }

    public StringFilter getIntroduction() {
        return introduction;
    }

    public StringFilter introduction() {
        if (introduction == null) {
            introduction = new StringFilter();
        }
        return introduction;
    }

    public void setIntroduction(StringFilter introduction) {
        this.introduction = introduction;
    }

    public StringFilter getPhoto() {
        return photo;
    }

    public StringFilter photo() {
        if (photo == null) {
            photo = new StringFilter();
        }
        return photo;
    }

    public void setPhoto(StringFilter photo) {
        this.photo = photo;
    }

    public StringFilter getBrokerName() {
        return brokerName;
    }

    public StringFilter brokerName() {
        if (brokerName == null) {
            brokerName = new StringFilter();
        }
        return brokerName;
    }

    public void setBrokerName(StringFilter brokerName) {
        this.brokerName = brokerName;
    }

    public StringFilter getBrokerContact() {
        return brokerContact;
    }

    public StringFilter brokerContact() {
        if (brokerContact == null) {
            brokerContact = new StringFilter();
        }
        return brokerContact;
    }

    public void setBrokerContact(StringFilter brokerContact) {
        this.brokerContact = brokerContact;
    }

    public StatusEnumFilter getStatus() {
        return status;
    }

    public StatusEnumFilter status() {
        if (status == null) {
            status = new StatusEnumFilter();
        }
        return status;
    }

    public void setStatus(StatusEnumFilter status) {
        this.status = status;
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

    public ZonedDateTimeFilter getApprovedAt() {
        return approvedAt;
    }

    public ZonedDateTimeFilter approvedAt() {
        if (approvedAt == null) {
            approvedAt = new ZonedDateTimeFilter();
        }
        return approvedAt;
    }

    public void setApprovedAt(ZonedDateTimeFilter approvedAt) {
        this.approvedAt = approvedAt;
    }

    public ZonedDateTimeFilter getRejectedAt() {
        return rejectedAt;
    }

    public ZonedDateTimeFilter rejectedAt() {
        if (rejectedAt == null) {
            rejectedAt = new ZonedDateTimeFilter();
        }
        return rejectedAt;
    }

    public void setRejectedAt(ZonedDateTimeFilter rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public StringFilter getRejectReason() {
        return rejectReason;
    }

    public StringFilter rejectReason() {
        if (rejectReason == null) {
            rejectReason = new StringFilter();
        }
        return rejectReason;
    }

    public void setRejectReason(StringFilter rejectReason) {
        this.rejectReason = rejectReason;
    }

    public StringFilter getApprovedBy() {
        return approvedBy;
    }

    public StringFilter approvedBy() {
        if (approvedBy == null) {
            approvedBy = new StringFilter();
        }
        return approvedBy;
    }

    public void setApprovedBy(StringFilter approvedBy) {
        this.approvedBy = approvedBy;
    }

    public StringFilter getRejectedBy() {
        return rejectedBy;
    }

    public StringFilter rejectedBy() {
        if (rejectedBy == null) {
            rejectedBy = new StringFilter();
        }
        return rejectedBy;
    }

    public void setRejectedBy(StringFilter rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public ActionEnumFilter getAction() {
        return action;
    }

    public ActionEnumFilter action() {
        if (action == null) {
            action = new ActionEnumFilter();
        }
        return action;
    }

    public void setAction(ActionEnumFilter action) {
        this.action = action;
    }

    public LongFilter getSocialLinkId() {
        return socialLinkId;
    }

    public LongFilter socialLinkId() {
        if (socialLinkId == null) {
            socialLinkId = new LongFilter();
        }
        return socialLinkId;
    }

    public void setSocialLinkId(LongFilter socialLinkId) {
        this.socialLinkId = socialLinkId;
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
        final ChatRoomCriteria that = (ChatRoomCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(groupName, that.groupName) &&
            Objects.equals(groupOwner, that.groupOwner) &&
            Objects.equals(introduction, that.introduction) &&
            Objects.equals(photo, that.photo) &&
            Objects.equals(brokerName, that.brokerName) &&
            Objects.equals(brokerContact, that.brokerContact) &&
            Objects.equals(status, that.status) &&
            Objects.equals(createdBy, that.createdBy) &&
            Objects.equals(createdAt, that.createdAt) &&
            Objects.equals(updatedAt, that.updatedAt) &&
            Objects.equals(approvedAt, that.approvedAt) &&
            Objects.equals(rejectedAt, that.rejectedAt) &&
            Objects.equals(rejectReason, that.rejectReason) &&
            Objects.equals(approvedBy, that.approvedBy) &&
            Objects.equals(rejectedBy, that.rejectedBy) &&
            Objects.equals(action, that.action) &&
            Objects.equals(socialLinkId, that.socialLinkId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            groupName,
            groupOwner,
            introduction,
            photo,
            brokerName,
            brokerContact,
            status,
            createdBy,
            createdAt,
            updatedAt,
            approvedAt,
            rejectedAt,
            rejectReason,
            approvedBy,
            rejectedBy,
            action,
            socialLinkId,
            distinct
        );
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ChatRoomCriteria{" +
            (id != null ? "id=" + id + ", " : "") +
            (groupName != null ? "groupName=" + groupName + ", " : "") +
            (groupOwner != null ? "groupOwner=" + groupOwner + ", " : "") +
            (introduction != null ? "introduction=" + introduction + ", " : "") +
            (photo != null ? "photo=" + photo + ", " : "") +
            (brokerName != null ? "brokerName=" + brokerName + ", " : "") +
            (brokerContact != null ? "brokerContact=" + brokerContact + ", " : "") +
            (status != null ? "status=" + status + ", " : "") +
            (createdBy != null ? "createdBy=" + createdBy + ", " : "") +
            (createdAt != null ? "createdAt=" + createdAt + ", " : "") +
            (updatedAt != null ? "updatedAt=" + updatedAt + ", " : "") +
            (approvedAt != null ? "approvedAt=" + approvedAt + ", " : "") +
            (rejectedAt != null ? "rejectedAt=" + rejectedAt + ", " : "") +
            (rejectReason != null ? "rejectReason=" + rejectReason + ", " : "") +
            (approvedBy != null ? "approvedBy=" + approvedBy + ", " : "") +
            (rejectedBy != null ? "rejectedBy=" + rejectedBy + ", " : "") +
            (action != null ? "action=" + action + ", " : "") +
            (socialLinkId != null ? "socialLinkId=" + socialLinkId + ", " : "") +
            (distinct != null ? "distinct=" + distinct + ", " : "") +
            "}";
    }
}
