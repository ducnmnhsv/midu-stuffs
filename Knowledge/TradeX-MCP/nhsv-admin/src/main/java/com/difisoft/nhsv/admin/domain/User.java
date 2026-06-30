package com.difisoft.nhsv.admin.domain;

import com.difisoft.nhsv.admin.constant.Constants;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.hibernate.annotations.BatchSize;

/**
 * A user.
 */
@Entity
@Table(name = "t_user")
@Data
public class User extends AbstractAuditingEntity<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    @Column(length = 50, unique = true, nullable = false)
    private String login;

    @JsonIgnore
    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60, nullable = false)
    private String password;

    @Size(max = 50)
    @Column(name = "full_name", length = 50)
    private String fullName;

    @Email
    @Size(min = 5, max = 254)
    @Column(length = 254, unique = true)
    private String email;

    @NotNull
    @Column(nullable = false)
    private boolean activated = false;

    @Size(min = 2, max = 10)
    @Column(name = "lang_key", length = 10)
    private String langKey;

    @Size(max = 256)
    @Column(name = "image_url", length = 256)
    private String imageUrl;

    @Size(max = 20)
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    private String activationKey;

    @Size(max = 20)
    @Column(name = "reset_key", length = 20)
    @JsonIgnore
    private String resetKey;

    @Column(name = "reset_date")
    private ZonedDateTime resetDate = null;

    @Column(name = "deactivated_at")
    private ZonedDateTime deactivatedAt;

    @Column(name = "deactivated_By")
    private String deactivatedBy;

    @Column(name = "invited_by")
    private String invitedBy;

    private String introduction;

    private String photo;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "t_user_authority", joinColumns = {
            @JoinColumn(name = "user_id", referencedColumnName = "id") }, inverseJoinColumns = {
                    @JoinColumn(name = "authority_name", referencedColumnName = "name") })
    @BatchSize(size = 20)
    private Set<Authority> authorities = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        return id != null && id.equals(((User) o).id);
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
        return "User{" +
                "login='" + login + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", activated='" + activated + '\'' +
                ", langKey='" + langKey + '\'' +
                ", activationKey='" + activationKey + '\'' +
                "}";
    }
}
