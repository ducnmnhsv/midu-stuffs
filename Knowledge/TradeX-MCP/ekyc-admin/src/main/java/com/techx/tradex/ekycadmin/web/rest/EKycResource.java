package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.repository.EKycRepository;
import com.techx.tradex.ekycadmin.service.EKycService;
import com.techx.tradex.ekycadmin.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.techx.tradex.ekycadmin.domain.EKyc}.
 */
@RestController
@RequestMapping("/api")
public class EKycResource {

    private final Logger log = LoggerFactory.getLogger(EKycResource.class);

    private static final String ENTITY_NAME = "eKyc";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EKycService eKycService;

    private final EKycRepository eKycRepository;

    public EKycResource(EKycService eKycService, EKycRepository eKycRepository) {
        this.eKycService = eKycService;
        this.eKycRepository = eKycRepository;
    }

    /**
     * {@code POST  /e-kycs} : Create a new eKyc.
     *
     * @param eKyc the eKyc to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eKyc, or with status {@code 400 (Bad Request)} if the eKyc has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/e-kycs")
    public ResponseEntity<EKyc> createEKyc(@Valid @RequestBody EKyc eKyc) throws URISyntaxException {
        log.debug("REST request to save EKyc : {}", eKyc);
        if (eKyc.getId() != null) {
            throw new BadRequestAlertException("A new eKyc cannot already have an ID", ENTITY_NAME, "idexists");
        }
        EKyc result = eKycService.save(eKyc);
        return ResponseEntity
            .created(new URI("/api/e-kycs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /e-kycs/:id} : Updates an existing eKyc.
     *
     * @param id the id of the eKyc to save.
     * @param eKyc the eKyc to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eKyc,
     * or with status {@code 400 (Bad Request)} if the eKyc is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eKyc couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/e-kycs/{id}")
    public ResponseEntity<EKyc> updateEKyc(@PathVariable(value = "id", required = false) final Long id, @Valid @RequestBody EKyc eKyc)
        throws URISyntaxException {
        log.debug("REST request to update EKyc : {}, {}", id, eKyc);
        if (eKyc.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eKyc.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eKycRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        EKyc result = eKycService.save(eKyc);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eKyc.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /e-kycs/:id} : Partial updates given fields of an existing eKyc, field will ignore if it is null
     *
     * @param id the id of the eKyc to save.
     * @param eKyc the eKyc to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eKyc,
     * or with status {@code 400 (Bad Request)} if the eKyc is not valid,
     * or with status {@code 404 (Not Found)} if the eKyc is not found,
     * or with status {@code 500 (Internal Server Error)} if the eKyc couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/e-kycs/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<EKyc> partialUpdateEKyc(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EKyc eKyc
    ) throws URISyntaxException {
        log.debug("REST request to partial update EKyc partially : {}, {}", id, eKyc);
        if (eKyc.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eKyc.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eKycRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EKyc> result = eKycService.partialUpdate(eKyc);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eKyc.getId().toString())
        );
    }

    /**
     * {@code GET  /e-kycs} : get all the eKycs.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eKycs in body.
     */
    @GetMapping("/e-kycs")
    public List<EKyc> getAllEKycs() {
        log.debug("REST request to get all EKycs");
        return eKycService.findAll();
    }

    /**
     * {@code GET  /e-kycs/:id} : get the "id" eKyc.
     *
     * @param id the id of the eKyc to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eKyc, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/e-kycs/{id}")
    public ResponseEntity<EKyc> getEKyc(@PathVariable Long id) {
        log.debug("REST request to get EKyc : {}", id);
        Optional<EKyc> eKyc = eKycService.findOne(id);
        return ResponseUtil.wrapOrNotFound(eKyc);
    }

    /**
     * {@code DELETE  /e-kycs/:id} : delete the "id" eKyc.
     *
     * @param id the id of the eKyc to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/e-kycs/{id}")
    public ResponseEntity<Void> deleteEKyc(@PathVariable Long id) {
        log.debug("REST request to delete EKyc : {}", id);
        eKycService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code SEARCH  /_search/e-kycs?query=:query} : search for the eKyc corresponding
     * to the query.
     *
     * @param query the query of the eKyc search.
     * @return the result of the search.
     */
    @GetMapping("/_search/e-kycs")
    public List<EKyc> searchEKycs(@RequestParam String query) {
        log.debug("REST request to search EKycs for query {}", query);
        return eKycService.search(query);
    }
}
