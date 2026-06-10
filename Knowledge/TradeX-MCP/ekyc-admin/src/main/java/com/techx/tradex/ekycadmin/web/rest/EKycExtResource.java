package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.domain.EKycExt;
import com.techx.tradex.ekycadmin.repository.EKycExtRepository;
import com.techx.tradex.ekycadmin.service.EKycExtService;
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
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.techx.tradex.ekycadmin.domain.EKycExt}.
 */
@RestController
@RequestMapping("/api")
public class EKycExtResource {

    private final Logger log = LoggerFactory.getLogger(EKycExtResource.class);

    private static final String ENTITY_NAME = "eKycExt";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EKycExtService eKycExtService;

    private final EKycExtRepository eKycExtRepository;

    public EKycExtResource(EKycExtService eKycExtService, EKycExtRepository eKycExtRepository) {
        this.eKycExtService = eKycExtService;
        this.eKycExtRepository = eKycExtRepository;
    }

    /**
     * {@code POST  /e-kyc-exts} : Create a new eKycExt.
     *
     * @param eKycExt the eKycExt to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eKycExt, or with status {@code 400 (Bad Request)} if the eKycExt has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/e-kyc-exts")
    public ResponseEntity<EKycExt> createEKycExt(@RequestBody EKycExt eKycExt) throws URISyntaxException {
        log.debug("REST request to save EKycExt : {}", eKycExt);
        if (eKycExt.getId() != null) {
            throw new BadRequestAlertException("A new eKycExt cannot already have an ID", ENTITY_NAME, "idexists");
        }
        EKycExt result = eKycExtService.save(eKycExt);
        return ResponseEntity
            .created(new URI("/api/e-kyc-exts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /e-kyc-exts/:id} : Updates an existing eKycExt.
     *
     * @param id the id of the eKycExt to save.
     * @param eKycExt the eKycExt to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eKycExt,
     * or with status {@code 400 (Bad Request)} if the eKycExt is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eKycExt couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/e-kyc-exts/{id}")
    public ResponseEntity<EKycExt> updateEKycExt(@PathVariable(value = "id", required = false) final Long id, @RequestBody EKycExt eKycExt)
        throws URISyntaxException {
        log.debug("REST request to update EKycExt : {}, {}", id, eKycExt);
        if (eKycExt.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eKycExt.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eKycExtRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        EKycExt result = eKycExtService.save(eKycExt);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eKycExt.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /e-kyc-exts/:id} : Partial updates given fields of an existing eKycExt, field will ignore if it is null
     *
     * @param id the id of the eKycExt to save.
     * @param eKycExt the eKycExt to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eKycExt,
     * or with status {@code 400 (Bad Request)} if the eKycExt is not valid,
     * or with status {@code 404 (Not Found)} if the eKycExt is not found,
     * or with status {@code 500 (Internal Server Error)} if the eKycExt couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/e-kyc-exts/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<EKycExt> partialUpdateEKycExt(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody EKycExt eKycExt
    ) throws URISyntaxException {
        log.debug("REST request to partial update EKycExt partially : {}, {}", id, eKycExt);
        if (eKycExt.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eKycExt.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eKycExtRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EKycExt> result = eKycExtService.partialUpdate(eKycExt);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eKycExt.getId().toString())
        );
    }

    /**
     * {@code GET  /e-kyc-exts} : get all the eKycExts.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eKycExts in body.
     */
    @GetMapping("/e-kyc-exts")
    public List<EKycExt> getAllEKycExts() {
        log.debug("REST request to get all EKycExts");
        return eKycExtService.findAll();
    }

    /**
     * {@code GET  /e-kyc-exts/:id} : get the "id" eKycExt.
     *
     * @param id the id of the eKycExt to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eKycExt, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/e-kyc-exts/{id}")
    public ResponseEntity<EKycExt> getEKycExt(@PathVariable Long id) {
        log.debug("REST request to get EKycExt : {}", id);
        Optional<EKycExt> eKycExt = eKycExtService.findOne(id);
        return ResponseUtil.wrapOrNotFound(eKycExt);
    }

    /**
     * {@code DELETE  /e-kyc-exts/:id} : delete the "id" eKycExt.
     *
     * @param id the id of the eKycExt to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/e-kyc-exts/{id}")
    public ResponseEntity<Void> deleteEKycExt(@PathVariable Long id) {
        log.debug("REST request to delete EKycExt : {}", id);
        eKycExtService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
