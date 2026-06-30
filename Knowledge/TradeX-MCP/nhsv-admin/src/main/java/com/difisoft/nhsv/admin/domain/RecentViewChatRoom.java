package com.difisoft.nhsv.admin.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;

/**
 * A RecentViewChatRoom.
 */
@Entity
@Table(name = "t_recent_view_chat_room")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class RecentViewChatRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "chat_room_id")
    private Long chatRoomId;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public RecentViewChatRoom id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return this.userId;
    }

    public RecentViewChatRoom userId(Long userId) {
        this.setUserId(userId);
        return this;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getChatRoomId() {
        return this.chatRoomId;
    }

    public RecentViewChatRoom chatRoomId(Long chatRoomId) {
        this.setChatRoomId(chatRoomId);
        return this;
    }

    public void setChatRoomId(Long chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public RecentViewChatRoom createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public RecentViewChatRoom updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ZonedDateTime getDeletedAt() {
        return this.deletedAt;
    }

    public RecentViewChatRoom deletedAt(ZonedDateTime deletedAt) {
        this.setDeletedAt(deletedAt);
        return this;
    }

    public void setDeletedAt(ZonedDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RecentViewChatRoom)) {
            return false;
        }
        return id != null && id.equals(((RecentViewChatRoom) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "RecentViewChatRoom{" +
            "id=" + getId() +
            ", userId=" + getUserId() +
            ", chatRoomId=" + getChatRoomId() +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", deletedAt='" + getDeletedAt() + "'" +
            "}";
    }
}
