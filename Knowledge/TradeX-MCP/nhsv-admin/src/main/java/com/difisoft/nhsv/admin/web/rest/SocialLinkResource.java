package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.domain.SocialLink;
import com.difisoft.nhsv.admin.repository.SocialLinkRepository;
import com.difisoft.nhsv.admin.service.SocialLinkService;
import com.difisoft.nhsv.admin.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.SocialLink}.
 */
@RestController
@RequestMapping("/api")
public class SocialLinkResource {

    private final Logger log = LoggerFactory.getLogger(SocialLinkResource.class);

    private static final String ENTITY_NAME = "socialLink";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SocialLinkService socialLinkService;

    private final SocialLinkRepository socialLinkRepository;

    public SocialLinkResource(SocialLinkService socialLinkService, SocialLinkRepository socialLinkRepository) {
        this.socialLinkService = socialLinkService;
        this.socialLinkRepository = socialLinkRepository;
    }

    /**
     * {@code POST  /social-links} : Create a new socialLink.
     *
     * @param socialLink the socialLink to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new socialLink, or with status {@code 400 (Bad Request)} if the socialLink has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/social-links")
    public ResponseEntity<SocialLink> createSocialLink(@RequestBody SocialLink socialLink) throws URISyntaxException {
        log.debug("REST request to save SocialLink : {}", socialLink);
        if (socialLink.getId() != null) {
            throw new BadRequestAlertException("A new socialLink cannot already have an ID", ENTITY_NAME, "idexists");
        }
        SocialLink result = socialLinkService.save(socialLink);
        return ResponseEntity
            .created(new URI("/api/social-links/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /social-links/:id} : Updates an existing socialLink.
     *
     * @param id the id of the socialLink to save.
     * @param socialLink the socialLink to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated socialLink,
     * or with status {@code 400 (Bad Request)} if the socialLink is not valid,
     * or with status {@code 500 (Internal Server Error)} if the socialLink couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/social-links/{id}")
    public ResponseEntity<SocialLink> updateSocialLink(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody SocialLink socialLink
    ) throws URISyntaxException {
        log.debug("REST request to update SocialLink : {}, {}", id, socialLink);
        if (socialLink.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, socialLink.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!socialLinkRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        SocialLink result = socialLinkService.update(socialLink);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, socialLink.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /social-links/:id} : Partial updates given fields of an existing socialLink, field will ignore if it is null
     *
     * @param id the id of the socialLink to save.
     * @param socialLink the socialLink to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated socialLink,
     * or with status {@code 400 (Bad Request)} if the socialLink is not valid,
     * or with status {@code 404 (Not Found)} if the socialLink is not found,
     * or with status {@code 500 (Internal Server Error)} if the socialLink couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/social-links/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<SocialLink> partialUpdateSocialLink(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody SocialLink socialLink
    ) throws URISyntaxException {
        log.debug("REST request to partial update SocialLink partially : {}, {}", id, socialLink);
        if (socialLink.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, socialLink.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!socialLinkRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<SocialLink> result = socialLinkService.partialUpdate(socialLink);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, socialLink.getId().toString())
        );
    }

    /**
     * {@code GET  /social-links} : get all the socialLinks.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of socialLinks in body.
     */
    @GetMapping("/social-links")
    public List<SocialLink> getAllSocialLinks() {
        log.debug("REST request to get all SocialLinks");
        return socialLinkService.findAll();
    }

    /**
     * {@code GET  /social-links/:id} : get the "id" socialLink.
     *
     * @param id the id of the socialLink to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the socialLink, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/social-links/{id}")
    public ResponseEntity<SocialLink> getSocialLink(@PathVariable Long id) {
        log.debug("REST request to get SocialLink : {}", id);
        Optional<SocialLink> socialLink = socialLinkService.findOne(id);
        return ResponseUtil.wrapOrNotFound(socialLink);
    }

    /**
     * {@code DELETE  /social-links/:id} : delete the "id" socialLink.
     *
     * @param id the id of the socialLink to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/social-links/{id}")
    public ResponseEntity<Void> deleteSocialLink(@PathVariable Long id) {
        log.debug("REST request to delete SocialLink : {}", id);
        socialLinkService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
