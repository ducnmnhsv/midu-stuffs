package com.difisoft.nhsv.admin.domain;

import com.difisoft.nhsv.admin.domain.enumeration.InviteStatusEnum;
import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;

/**
 * A InviteUser.
 */
@Entity
@Table(name = "t_invite_user")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class InviteUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "login")
    private String login;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InviteStatusEnum status;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "updated_at")
    private ZonedDateTime updatedAt;

    @Column(name = "created_id")
    private Long createdId;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "activation_key")
    private String activationKey;

    @Column(name = "activation_date")
    private ZonedDateTime activationDate;

    @Column(name = "lang_key")
    private String langKey;

    @Column(name = "authorities")
    private String authorities;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public InviteUser id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return this.login;
    }

    public InviteUser login(String login) {
        this.setLogin(login);
        return this;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return this.email;
    }

    public InviteUser email(String email) {
        this.setEmail(email);
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public InviteStatusEnum getStatus() {
        return this.status;
    }

    public InviteUser status(InviteStatusEnum status) {
        this.setStatus(status);
        return this;
    }

    public void setStatus(InviteStatusEnum status) {
        this.status = status;
    }

    public ZonedDateTime getCreatedAt() {
        return this.createdAt;
    }

    public InviteUser createdAt(ZonedDateTime createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public InviteUser updatedAt(ZonedDateTime updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedId() {
        return this.createdId;
    }

    public InviteUser createdId(Long createdId) {
        this.setCreatedId(createdId);
        return this;
    }

    public void setCreatedId(Long createdId) {
        this.createdId = createdId;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public InviteUser createdBy(String createdBy) {
        this.setCreatedBy(createdBy);
        return this;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getActivationKey() {
        return this.activationKey;
    }

    public InviteUser activationKey(String activationKey) {
        this.setActivationKey(activationKey);
        return this;
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    public ZonedDateTime getActivationDate() {
        return this.activationDate;
    }

    public InviteUser activationDate(ZonedDateTime activationDate) {
        this.setActivationDate(activationDate);
        return this;
    }

    public void setActivationDate(ZonedDateTime activationDate) {
        this.activationDate = activationDate;
    }

    public String getLangKey() {
        return this.langKey;
    }

    public InviteUser langKey(String langKey) {
        this.setLangKey(langKey);
        return this;
    }

    public void setLangKey(String langKey) {
        this.langKey = langKey;
    }

    public String getAuthorities() {
        return this.authorities;
    }

    public InviteUser authorities(String authorities) {
        this.setAuthorities(authorities);
        return this;
    }

    public void setAuthorities(String authorities) {
        this.authorities = authorities;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InviteUser)) {
            return false;
        }
        return id != null && id.equals(((InviteUser) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "InviteUser{" +
            "id=" + getId() +
            ", login='" + getLogin() + "'" +
            ", email='" + getEmail() + "'" +
            ", status='" + getStatus() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            ", createdId=" + getCreatedId() +
            ", createdBy='" + getCreatedBy() + "'" +
            ", activationKey='" + getActivationKey() + "'" +
            ", activationDate='" + getActivationDate() + "'" +
            ", langKey='" + getLangKey() + "'" +
            ", authorities='" + getAuthorities() + "'" +
            "}";
    }
}
