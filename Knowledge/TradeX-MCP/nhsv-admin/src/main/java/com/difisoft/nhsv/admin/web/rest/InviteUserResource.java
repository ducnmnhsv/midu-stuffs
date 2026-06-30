package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.domain.InviteUser;
import com.difisoft.nhsv.admin.repository.InviteUserRepository;
import com.difisoft.nhsv.admin.service.InviteUserQueryService;
import com.difisoft.nhsv.admin.service.InviteUserService;
import com.difisoft.nhsv.admin.service.criteria.InviteUserCriteria;
import com.difisoft.nhsv.admin.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.InviteUser}.
 */
@RestController
@RequestMapping("/api")
public class InviteUserResource {

    private final Logger log = LoggerFactory.getLogger(InviteUserResource.class);

    private static final String ENTITY_NAME = "inviteUser";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final InviteUserService inviteUserService;

    private final InviteUserRepository inviteUserRepository;

    private final InviteUserQueryService inviteUserQueryService;

    public InviteUserResource(
        InviteUserService inviteUserService,
        InviteUserRepository inviteUserRepository,
        InviteUserQueryService inviteUserQueryService
    ) {
        this.inviteUserService = inviteUserService;
        this.inviteUserRepository = inviteUserRepository;
        this.inviteUserQueryService = inviteUserQueryService;
    }

    /**
     * {@code POST  /invite-users} : Create a new inviteUser.
     *
     * @param inviteUser the inviteUser to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new inviteUser, or with status {@code 400 (Bad Request)} if the inviteUser has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/invite-users")
    public ResponseEntity<InviteUser> createInviteUser(@RequestBody InviteUser inviteUser) throws URISyntaxException {
        log.debug("REST request to save InviteUser : {}", inviteUser);
        if (inviteUser.getId() != null) {
            throw new BadRequestAlertException("A new inviteUser cannot already have an ID", ENTITY_NAME, "idexists");
        }
        InviteUser result = inviteUserService.save(inviteUser);
        return ResponseEntity
            .created(new URI("/api/invite-users/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /invite-users/:id} : Updates an existing inviteUser.
     *
     * @param id the id of the inviteUser to save.
     * @param inviteUser the inviteUser to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated inviteUser,
     * or with status {@code 400 (Bad Request)} if the inviteUser is not valid,
     * or with status {@code 500 (Internal Server Error)} if the inviteUser couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/invite-users/{id}")
    public ResponseEntity<InviteUser> updateInviteUser(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody InviteUser inviteUser
    ) throws URISyntaxException {
        log.debug("REST request to update InviteUser : {}, {}", id, inviteUser);
        if (inviteUser.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, inviteUser.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!inviteUserRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        InviteUser result = inviteUserService.update(inviteUser);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, inviteUser.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /invite-users/:id} : Partial updates given fields of an existing inviteUser, field will ignore if it is null
     *
     * @param id the id of the inviteUser to save.
     * @param inviteUser the inviteUser to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated inviteUser,
     * or with status {@code 400 (Bad Request)} if the inviteUser is not valid,
     * or with status {@code 404 (Not Found)} if the inviteUser is not found,
     * or with status {@code 500 (Internal Server Error)} if the inviteUser couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/invite-users/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<InviteUser> partialUpdateInviteUser(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody InviteUser inviteUser
    ) throws URISyntaxException {
        log.debug("REST request to partial update InviteUser partially : {}, {}", id, inviteUser);
        if (inviteUser.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, inviteUser.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!inviteUserRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<InviteUser> result = inviteUserService.partialUpdate(inviteUser);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, inviteUser.getId().toString())
        );
    }

    /**
     * {@code GET  /invite-users} : get all the inviteUsers.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of inviteUsers in body.
     */
    @GetMapping("/invite-users")
    public ResponseEntity<List<InviteUser>> getAllInviteUsers(
        InviteUserCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get InviteUsers by criteria: {}", criteria);
        Page<InviteUser> page = inviteUserQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /invite-users/count} : count all the inviteUsers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/invite-users/count")
    public ResponseEntity<Long> countInviteUsers(InviteUserCriteria criteria) {
        log.debug("REST request to count InviteUsers by criteria: {}", criteria);
        return ResponseEntity.ok().body(inviteUserQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /invite-users/:id} : get the "id" inviteUser.
     *
     * @param id the id of the inviteUser to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the inviteUser, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/invite-users/{id}")
    public ResponseEntity<InviteUser> getInviteUser(@PathVariable Long id) {
        log.debug("REST request to get InviteUser : {}", id);
        Optional<InviteUser> inviteUser = inviteUserService.findOne(id);
        return ResponseUtil.wrapOrNotFound(inviteUser);
    }

    /**
     * {@code DELETE  /invite-users/:id} : delete the "id" inviteUser.
     *
     * @param id the id of the inviteUser to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/invite-users/{id}")
    public ResponseEntity<Void> deleteInviteUser(@PathVariable Long id) {
        log.debug("REST request to delete InviteUser : {}", id);
        inviteUserService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
