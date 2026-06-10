package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.domain.PublicCoop;
import com.techx.tradex.ekycadmin.repository.PublicCoopRepository;
import com.techx.tradex.ekycadmin.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.techx.tradex.ekycadmin.domain.PublicCoop}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class PublicCoopResource {

    private final Logger log = LoggerFactory.getLogger(PublicCoopResource.class);

    private static final String ENTITY_NAME = "publicCoop";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final PublicCoopRepository publicCoopRepository;

    public PublicCoopResource(PublicCoopRepository publicCoopRepository) {
        this.publicCoopRepository = publicCoopRepository;
    }

    /**
     * {@code POST  /public-coops} : Create a new publicCoop.
     *
     * @param publicCoop the publicCoop to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new publicCoop, or with status {@code 400 (Bad Request)} if the publicCoop has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/public-coops")
    public ResponseEntity<PublicCoop> createPublicCoop(@RequestBody PublicCoop publicCoop) throws URISyntaxException {
        log.debug("REST request to save PublicCoop : {}", publicCoop);
        if (publicCoop.getId() != null) {
            throw new BadRequestAlertException("A new publicCoop cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PublicCoop result = publicCoopRepository.save(publicCoop);
        return ResponseEntity
            .created(new URI("/api/public-coops/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /public-coops/:id} : Updates an existing publicCoop.
     *
     * @param id the id of the publicCoop to save.
     * @param publicCoop the publicCoop to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated publicCoop,
     * or with status {@code 400 (Bad Request)} if the publicCoop is not valid,
     * or with status {@code 500 (Internal Server Error)} if the publicCoop couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/public-coops/{id}")
    public ResponseEntity<PublicCoop> updatePublicCoop(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody PublicCoop publicCoop
    ) throws URISyntaxException {
        log.debug("REST request to update PublicCoop : {}, {}", id, publicCoop);
        if (publicCoop.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, publicCoop.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!publicCoopRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        PublicCoop result = publicCoopRepository.save(publicCoop);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, publicCoop.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /public-coops/:id} : Partial updates given fields of an existing publicCoop, field will ignore if it is null
     *
     * @param id the id of the publicCoop to save.
     * @param publicCoop the publicCoop to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated publicCoop,
     * or with status {@code 400 (Bad Request)} if the publicCoop is not valid,
     * or with status {@code 404 (Not Found)} if the publicCoop is not found,
     * or with status {@code 500 (Internal Server Error)} if the publicCoop couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/public-coops/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<PublicCoop> partialUpdatePublicCoop(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody PublicCoop publicCoop
    ) throws URISyntaxException {
        log.debug("REST request to partial update PublicCoop partially : {}, {}", id, publicCoop);
        if (publicCoop.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, publicCoop.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!publicCoopRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<PublicCoop> result = publicCoopRepository
            .findById(publicCoop.getId())
            .map(
                existingPublicCoop -> {
                    if (publicCoop.getCompanyName() != null) {
                        existingPublicCoop.setCompanyName(publicCoop.getCompanyName());
                    }
                    if (publicCoop.getStock() != null) {
                        existingPublicCoop.setStock(publicCoop.getStock());
                    }
                    if (publicCoop.getPosition() != null) {
                        existingPublicCoop.setPosition(publicCoop.getPosition());
                    }

                    return existingPublicCoop;
                }
            )
            .map(publicCoopRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, publicCoop.getId().toString())
        );
    }

    /**
     * {@code GET  /public-coops} : get all the publicCoops.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of publicCoops in body.
     */
    @GetMapping("/public-coops")
    public List<PublicCoop> getAllPublicCoops() {
        log.debug("REST request to get all PublicCoops");
        return publicCoopRepository.findAll();
    }

    /**
     * {@code GET  /public-coops/:id} : get the "id" publicCoop.
     *
     * @param id the id of the publicCoop to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the publicCoop, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/public-coops/{id}")
    public ResponseEntity<PublicCoop> getPublicCoop(@PathVariable Long id) {
        log.debug("REST request to get PublicCoop : {}", id);
        Optional<PublicCoop> publicCoop = publicCoopRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(publicCoop);
    }

    /**
     * {@code DELETE  /public-coops/:id} : delete the "id" publicCoop.
     *
     * @param id the id of the publicCoop to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/public-coops/{id}")
    public ResponseEntity<Void> deletePublicCoop(@PathVariable Long id) {
        log.debug("REST request to delete PublicCoop : {}", id);
        publicCoopRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
