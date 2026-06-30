package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.domain.InviteUser;
import com.difisoft.nhsv.admin.domain.enumeration.InviteStatusEnum;
import com.difisoft.nhsv.admin.repository.UserRepository;
import com.difisoft.nhsv.admin.repository.primary.InviteUserPrimaryRepository;
import com.difisoft.nhsv.admin.security.AuthoritiesConstants;
import com.difisoft.nhsv.admin.security.SecurityUtils;
import com.difisoft.nhsv.admin.service.InviteUserQueryPrimaryService;
import com.difisoft.nhsv.admin.service.InviteUserServiceImp;
import com.difisoft.nhsv.admin.service.MailService;
import com.difisoft.nhsv.admin.service.criteria.InviteUserPrimaryCriteria;
import com.difisoft.nhsv.admin.web.rest.errors.BadRequestAlertException;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import tech.jhipster.security.RandomUtil;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing
 * {@link com.difisoft.nhsv.admin.domain.InviteUser}.
 */
@RestController
@RequestMapping("/api/v2")
public class InviteUserPrimaryResource {

    private final Logger log = LoggerFactory.getLogger(InviteUserResource.class);

    private static final String ENTITY_NAME = "inviteUser";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InviteUserServiceImp inviteUserService;

    private final InviteUserPrimaryRepository inviteUserRepository;

    private final InviteUserQueryPrimaryService inviteUserQueryService;

    private final UserRepository userRepository;

    private final MailService mailService;

    public InviteUserPrimaryResource(
            InviteUserServiceImp inviteUserService,
            InviteUserPrimaryRepository inviteUserRepository,
            InviteUserQueryPrimaryService inviteUserQueryService,
            UserRepository userRepository,
            MailService mailService) {
        this.inviteUserService = inviteUserService;
        this.inviteUserRepository = inviteUserRepository;
        this.inviteUserQueryService = inviteUserQueryService;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    @GetMapping("/invite-users")
    public ResponseEntity<List<InviteUser>> getAllInviteUsers(
            InviteUserPrimaryCriteria criteria,
            @org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get InviteUsers by criteria: {}", criteria);
        Pageable pageAble = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by("createdAt").descending());
        Page<InviteUser> page = inviteUserQueryService.findByCriteria(criteria, pageAble);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/invite-users/{id}")
    public ResponseEntity<InviteUser> getInviteUser(@PathVariable Long id) {
        log.debug("REST request to get InviteUser : {}", id);
        Optional<InviteUser> inviteUser = inviteUserService.findOne(id);
        return ResponseUtil.wrapOrNotFound(inviteUser);
    }

    @PostMapping("/invite-users/invite")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + " \" , \"" + AuthoritiesConstants.SUPER_ADMIN
            + "\")")
    public ResponseEntity<InviteUser> inviteUser(@Valid @RequestBody InviteUser inviteUser) {
        log.info("REST request to invite User : {}", inviteUser);
        if (inviteUser.getStatus() != null) {
            if (inviteUser.getStatus().equals(InviteStatusEnum.ACCOUNT_CREATED)) {
                throw new BadRequestAlertException("User already accepted the invite", ENTITY_NAME, "useraccepted");
            }
        }
        if (userRepository.findOneByEmailIgnoreCase(inviteUser.getEmail()).isPresent()) {
            InviteUser user = inviteUserRepository.findByEmailIgnoreCase(inviteUser.getEmail()).orElse(null);
            if (user != null && user.getStatus().equals(InviteStatusEnum.ACCOUNT_CREATED)) {
                throw new BadRequestAlertException("User already accepted the invite", ENTITY_NAME, "useraccepted");
            }
        }
        boolean isLoginDeactive = userRepository
                .findOneByEmailIgnoreCaseAndLoginAndActivatedIsFalse(inviteUser.getEmail(), inviteUser.getLogin())
                .isPresent();
        if (userRepository.findOneByLogin(inviteUser.getLogin()).isPresent() && !isLoginDeactive) {
            throw new BadRequestAlertException("Username already in use", ENTITY_NAME, "Username in use");
        }
        InviteUser exInviteUserEmail = inviteUserRepository.findByEmailIgnoreCase(inviteUser.getEmail()).orElse(null);
        InviteUser exInviteUserLogin = inviteUserRepository.findByLogin(inviteUser.getLogin()).orElse(null);
        if (exInviteUserEmail != null) {
            inviteUser.setId(exInviteUserEmail.getId());
        }
        if (exInviteUserLogin != null && exInviteUserLogin.getId() != inviteUser.getId()) {
            throw new BadRequestAlertException("Username already in use", ENTITY_NAME, "Username in use");
        }
        inviteUser.setCreatedId(exInviteUserEmail != null ? exInviteUserEmail.getCreatedId() : null);
        inviteUser.setCreatedBy(SecurityUtils.getCurrentUserLogin().get());
        inviteUser.setCreatedAt(ZonedDateTime.now());
        inviteUser.setStatus(InviteStatusEnum.PENDING);
        inviteUser.setActivationDate(ZonedDateTime.now());
        inviteUser.activationKey(RandomUtil.generateActivationKey());
        inviteUser.setUpdatedAt(ZonedDateTime.now());
        InviteUser user = inviteUserService.save(inviteUser);
        mailService.sendCreationEmail(user);
        return ResponseEntity
                .created(URI.create("/api/invite-users/" + user.getId()))
                .headers(HeaderUtil.createAlert(applicationName, "Invitation Link has been sent", ENTITY_NAME))
                .body(user);
    }

    @PostMapping("/invite-users/{id}/resend")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + " \" , \"" + AuthoritiesConstants.SUPER_ADMIN
            + "\")")
    public ResponseEntity<InviteUser> reSendInvite(@PathVariable Long id) {
        log.info("REST request to resend invite User : {}", id);
        InviteUser inviteUser = inviteUserService.findOne(id).orElse(null);
        if (inviteUser == null) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (inviteUser.getStatus().equals(InviteStatusEnum.ACCOUNT_CREATED)) {
            throw new BadRequestAlertException("User already accepted the invite", ENTITY_NAME, "useraccepted");
        }
        if (userRepository.findOneByEmailIgnoreCase(inviteUser.getEmail()).isPresent() &&
                inviteUser.getStatus() != InviteStatusEnum.ACCOUNT_DEACTIVATED &&
                inviteUser.getStatus() != InviteStatusEnum.PENDING) {
            throw new BadRequestAlertException("Email already in use", ENTITY_NAME, "email in use");
        }
        boolean isLoginDeactive = userRepository
                .findOneByEmailIgnoreCaseAndLoginAndActivatedIsFalse(inviteUser.getEmail(), inviteUser.getLogin())
                .isPresent();
        if (userRepository.findOneByLogin(inviteUser.getLogin()).isPresent() && !isLoginDeactive) {
            throw new BadRequestAlertException("Username already in use", ENTITY_NAME, "Username in use");
        }
        inviteUser.setCreatedBy(SecurityUtils.getCurrentUserLogin().get());
        inviteUser.setUpdatedAt(ZonedDateTime.now());
        inviteUser.setStatus(InviteStatusEnum.PENDING);
        inviteUser.setActivationDate(ZonedDateTime.now());
        inviteUser.activationKey(RandomUtil.generateActivationKey());
        InviteUser user = inviteUserService.save(inviteUser);
        mailService.sendCreationEmail(user);
        return ResponseEntity
                .created(URI.create("/api/invite-users/" + user.getId()))
                .headers(HeaderUtil.createAlert(applicationName, "Invitation Link has been resent",
                        inviteUser.getEmail()))
                .body(user);
    }
}
