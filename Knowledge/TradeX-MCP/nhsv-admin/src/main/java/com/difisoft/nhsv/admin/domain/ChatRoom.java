package com.difisoft.nhsv.admin.domain;

import com.difisoft.nhsv.admin.domain.enumeration.ActionEnum;
import com.difisoft.nhsv.admin.domain.enumeration.StatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * A ChatRoom.
 */
@Entity
@Table(name = "t_chat_room")
@SuppressWarnings("common-java:DuplicatedBlocks")
@Data
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "group_owner")
    private String groupOwner;

    @Column(name = "introduction")
    private String introduction;

    @Column(name = "photo")
    private String photo;

    @Column(name = "broker_name")
    private String brokerName;

    @Column(name = "broker_contact")
    private String brokerContact;

    @Column(name = "broker_photo")
    private String brokerPhoto;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusEnum status;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "approved_at")
    private ZonedDateTime approvedAt;

    @Column(name = "rejected_at")
    private ZonedDateTime rejectedAt;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "action")
    private ActionEnum action;

    @OneToMany(mappedBy = "chatRoom" , fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = { "chatRoom" }, allowSetters = true)
    private Set<SocialLink> socialLinks = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public ChatRoom id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupName() {
        return this.groupName;
    }

    public ChatRoom groupName(String groupName) {
        this.setGroupName(groupName);
        return this;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupOwner() {
        return this.groupOwner;
    }

    public ChatRoom groupOwner(String groupOwner) {
        this.setGroupOwner(groupOwner);
        return this;
    }

    public void setGroupOwner(String groupOwner) {
        this.groupOwner = groupOwner;
    }

    public String getIntroduction() {
        return this.introduction;
    }

    public ChatRoom introduction(String introduction) {
        this.setIntroduction(introduction);
        return this;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getPhoto() {
        return this.photo;
    }

    public ChatRoom photo(String photo) {
        this.setPhoto(photo);
        return this;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getBrokerName() {
        return this.brokerName;
    }

    public ChatRoom brokerName(String brokerName) {
        this.setBrokerName(brokerName);
        return this;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public String getBrokerContact() {
        return this.brokerContact;
    }

    public ChatRoom brokerContact(String brokerContact) {
        this.setBrokerContact(brokerContact);
        return this;
    }

    public void setBrokerContact(String brokerContact) {
        this.brokerContact = brokerContact;
    }

    public StatusEnum getStatus() {
        return this.status;
    }

    public ChatRoom status(StatusEnum status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public ChatRoom createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public ChatRoom createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public ChatRoom updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ZonedDateTime getApprovedAt() {
        return this.approvedAt;
    }

    public ChatRoom approvedAt(ZonedDateTime approvedAt) {
        this.setApprovedAt(approvedAt);
        return this;
    }

    public void setApprovedAt(ZonedDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public ZonedDateTime getRejectedAt() {
        return this.rejectedAt;
    }

    public ChatRoom rejectedAt(ZonedDateTime rejectedAt) {
        this.setRejectedAt(rejectedAt);
        return this;
    }

    public void setRejectedAt(ZonedDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public ChatRoom rejectReason(String rejectReason) {
        this.setRejectReason(rejectReason);
        return this;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getApprovedBy() {
        return this.approvedBy;
    }

    public ChatRoom approvedBy(String approvedBy) {
        this.setApprovedBy(approvedBy);
        return this;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getRejectedBy() {
        return this.rejectedBy;
    }

    public ChatRoom rejectedBy(String rejectedBy) {
        this.setRejectedBy(rejectedBy);
        return this;
    }

    public void setRejectedBy(String rejectedBy) {
        this.rejectedBy = rejectedBy;
    }

    public ActionEnum getAction() {
        return this.action;
    }

    public ChatRoom action(ActionEnum action) {
        this.setAction(action);
        return this;
    }

    public void setAction(ActionEnum action) {
        this.action = action;
    }

    public Set<SocialLink> getSocialLinks() {
        return this.socialLinks;
    }

    public void setSocialLinks(Set<SocialLink> socialLinks) {
        if (this.socialLinks != null) {
            this.socialLinks.forEach(i -> i.setChatRoom(null));
        }
        if (socialLinks != null) {
            socialLinks.forEach(i -> i.setChatRoom(this));
        }
        this.socialLinks = socialLinks;
    }

    public ChatRoom socialLinks(Set<SocialLink> socialLinks) {
        this.setSocialLinks(socialLinks);
        return this;
    }

    public ChatRoom addSocialLink(SocialLink socialLink) {
        this.socialLinks.add(socialLink);
        socialLink.setChatRoom(this);
        return this;
    }

    public ChatRoom removeSocialLink(SocialLink socialLink) {
        this.socialLinks.remove(socialLink);
        socialLink.setChatRoom(null);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatRoom)) {
            return false;
        }
        return id != null && id.equals(((ChatRoom) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ChatRoom{" +
            "id=" + getId() +
            ", groupName='" + getGroupName() + "'" +
            ", groupOwner='" + getGroupOwner() + "'" +
            ", introduction='" + getIntroduction() + "'" +
            ", photo='" + getPhoto() + "'" +
            ", brokerName='" + getBrokerName() + "'" +
            ", brokerContact='" + getBrokerContact() + "'" +
            ", status='" + getStatus() + "'" +
            ", createdBy='" + getCreatedBy() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", approvedAt='" + getApprovedAt() + "'" +
            ", rejectedAt='" + getRejectedAt() + "'" +
            ", rejectReason='" + getRejectReason() + "'" +
            ", approvedBy='" + getApprovedBy() + "'" +
            ", rejectedBy='" + getRejectedBy() + "'" +
            ", action='" + getAction() + "'" +
            "}";
    }
}
