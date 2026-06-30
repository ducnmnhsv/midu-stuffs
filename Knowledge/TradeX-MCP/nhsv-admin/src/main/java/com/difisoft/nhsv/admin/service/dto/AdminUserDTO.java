package com.difisoft.nhsv.admin.service.dto;

import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.Authority;
import com.difisoft.nhsv.admin.domain.User;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DTO representing a user, with his authorities.
 */
@Data
@NoArgsConstructor
public class AdminUserDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;
    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;
    @Size(max = 50)
    private String fullName;
    @Email
    @Size(min = 5, max = 254)
    private String email;
    @Size(max = 256)
    private String imageUrl;
    private boolean activated = false;
    @Size(min = 2, max = 10)
    private String langKey;
    private String createdBy;
    private ZonedDateTime createdDate;
    private String lastModifiedBy;
    private ZonedDateTime lastModifiedDate;
    private Set<String> authorities;
    private ZonedDateTime deactivatedAt;
    private String deactivatedBy;
    private String invitedBy;
    private String introduction;
    private String photoLink;
    private List<CopyMarketLeaderDetailsDTO> copyMarketLeaderDetailsDTO;

    public AdminUserDTO(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.activated = user.isActivated();
        this.imageUrl = user.getImageUrl();
        this.langKey = user.getLangKey();
        this.createdBy = user.getCreatedBy();
        this.createdDate = user.getCreatedDate();
        this.lastModifiedBy = user.getLastModifiedBy();
        this.lastModifiedDate = user.getLastModifiedDate();
        this.authorities = user.getAuthorities().stream().map(Authority::getName).collect(Collectors.toSet());
        this.deactivatedAt = user.getDeactivatedAt();
        this.deactivatedBy = user.getDeactivatedBy();
        this.invitedBy = user.getInvitedBy();
        this.introduction = user.getIntroduction();
        this.photoLink = user.getPhoto();
    }
}
