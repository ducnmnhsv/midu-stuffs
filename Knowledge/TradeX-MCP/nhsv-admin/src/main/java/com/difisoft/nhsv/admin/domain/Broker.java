package com.difisoft.nhsv.admin.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "t_broker")
@SuppressWarnings("common-java:DuplicatedBlocks")
@Data
public class Broker implements Serializable, Persistable<Long> {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "total_chat_room")
    private Long totalChatRoom;

    @Column(name = "total_viewed_chat_room")
    private Long totalViewdChatRoom;

    @Column(name = "current_rank")
    private Integer currentRank;

    @Column(name = "is_dynamic")
    private Boolean isDynamic;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "deactivated_at")
    private ZonedDateTime deactivatedAt;

    @Column(name = "deactivated_by")
    private String deactivatedBy;

    @Column(name = "invited_by")
    private String invitedBy;

    private String photo;

    private String introduction;

    @Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Broker id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return this.username;
    }

    public Broker username(String username) {
        this.setUsername(username);
        return this;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return this.fullname;
    }

    public Broker fullname(String fullname) {
        this.setFullname(fullname);
        return this;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Boolean getStatus() {
        return this.status;
    }

    public Broker status(Boolean status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Long getTotalChatRoom() {
        return this.totalChatRoom;
    }

    public Broker totalChatRoom(Long totalChatRoom) {
        this.setTotalChatRoom(totalChatRoom);
        return this;
    }

    public void setTotalChatRoom(Long totalChatRoom) {
        this.totalChatRoom = totalChatRoom;
    }

    public Integer getCurrentRank() {
        return this.currentRank;
    }

    public Broker currentRank(Integer currentRank) {
        this.setCurrentRank(currentRank);
        return this;
    }

    public void setCurrentRank(Integer currentRank) {
        this.currentRank = currentRank;
    }

    public Boolean getIsDynamic() {
        return this.isDynamic;
    }

    public Broker isDynamic(Boolean isDynamic) {
        this.setIsDynamic(isDynamic);
        return this;
    }

    public void setIsDynamic(Boolean isDynamic) {
        this.isDynamic = isDynamic;
    }

    public String getEmail() {
        return this.email;
    }

    public Broker email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public Broker createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public Broker updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ZonedDateTime getDeactivatedAt() {
        return this.deactivatedAt;
    }

    public Broker deactivatedAt(ZonedDateTime deactivatedAt) {
        this.setDeactivatedAt(deactivatedAt);
        return this;
    }

    public void setDeactivatedAt(ZonedDateTime deactivatedAt) {
        this.deactivatedAt = deactivatedAt;
    }

    public String getDeactivatedBy() {
        return this.deactivatedBy;
    }

    public Broker deactivatedBy(String deactivatedBy) {
        this.setDeactivatedBy(deactivatedBy);
        return this;
    }

    public void setDeactivatedBy(String deactivatedBy) {
        this.deactivatedBy = deactivatedBy;
    }

    public String getInvitedBy() {
        return this.invitedBy;
    }

    public Broker invitedBy(String invitedBy) {
        this.setInvitedBy(invitedBy);
        return this;
    }

    public void setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
    }

    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public Broker setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    @PostLoad
    @PostPersist
    public void updateEntityState() {
        this.setIsPersisted();
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and
    // setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Broker)) {
            return false;
        }
        return id != null && id.equals(((Broker) o).id);
    }

    @Override
    public int hashCode() {
        // see
        // https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Broker{" +
                "id=" + getId() +
                ", username='" + getUsername() + "'" +
                ", fullname='" + getFullname() + "'" +
                ", status='" + getStatus() + "'" +
                ", totalChatRoom=" + getTotalChatRoom() +
                ", currentRank=" + getCurrentRank() +
                ", isDynamic='" + getIsDynamic() + "'" +
                ", email='" + getEmail() + "'" +
                ", createdAt='" + getCreatedAt() + "'" +
                ", updatedAt='" + getUpdatedAt() + "'" +
                ", deactivatedAt='" + getDeactivatedAt() + "'" +
                ", deactivatedBy='" + getDeactivatedBy() + "'" +
                ", invitedBy='" + getInvitedBy() + "'" +
                "}";
    }
}
