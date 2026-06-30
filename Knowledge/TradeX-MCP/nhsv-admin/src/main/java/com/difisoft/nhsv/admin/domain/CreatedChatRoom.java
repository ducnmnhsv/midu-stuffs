package com.difisoft.nhsv.admin.domain;

import com.difisoft.nhsv.admin.domain.enumeration.StatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * A CreatedChatRoom.
 */
@Entity
@Table(name = "t_created_chat_room")
@SuppressWarnings("common-java:DuplicatedBlocks")
@Data
public class CreatedChatRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
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

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "total_view")
    private Long totalView;

    @Column(name = "broker_id")
    private Long brokerId;

    @Column(name = "rejected_at")
    private ZonedDateTime rejectedAt;

    @Column(name = "reject_reason")
    private String rejectReason;

    @Column(name = "rejected_by")
    private String rejectedBy;

    @OneToMany(mappedBy = "chatRoom", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnoreProperties(value = { "chatRoom" }, allowSetters = true)
    private Set<SocialLink> socialLinks = new HashSet<>();
    // jhipster-needle-entity-add-field - JHipster will add fields here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CreatedChatRoom)) {
            return false;
        }
        return id != null && id.equals(((CreatedChatRoom) o).id);
    }

    @Override
    public int hashCode() {
        // see
        // https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }
}
