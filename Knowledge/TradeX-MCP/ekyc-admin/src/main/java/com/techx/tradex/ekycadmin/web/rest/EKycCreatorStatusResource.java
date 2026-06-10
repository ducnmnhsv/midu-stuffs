package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.domain.EKycCreatorStatus;
import com.techx.tradex.ekycadmin.repository.EKycCreatorStatusRepository;
import com.techx.tradex.ekycadmin.repository.EKycRepository;
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
 * REST controller for managing {@link com.techx.tradex.ekycadmin.domain.EKycCreatorStatus}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class EKycCreatorStatusResource {

    private final Logger log = LoggerFactory.getLogger(EKycCreatorStatusResource.class);

    private static final String ENTITY_NAME = "eKycCreatorStatus";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EKycCreatorStatusRepository eKycCreatorStatusRepository;

    private final EKycRepository eKycRepository;

    public EKycCreatorStatusResource(EKycCreatorStatusRepository eKycCreatorStatusRepository, EKycRepository eKycRepository) {
        this.eKycCreatorStatusRepository = eKycCreatorStatusRepository;
        this.eKycRepository = eKycRepository;
    }

    /**
     * {@code POST  /e-kyc-creator-statuses} : Create a new eKycCreatorStatus.
     *
     * @param eKycCreatorStatus the eKycCreatorStatus to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eKycCreatorStatus, or with status {@code 400 (Bad Request)} if the eKycCreatorStatus has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/e-kyc-creator-statuses")
    public ResponseEntity<EKycCreatorStatus> createEKycCreatorStatus(@RequestBody EKycCreatorStatus eKycCreatorStatus)
        throws URISyntaxException {
        log.debug("REST request to save EKycCreatorStatus : {}", eKycCreatorStatus);
        if (eKycCreatorStatus.getId() != null) {
            throw new BadRequestAlertException("A new eKycCreatorStatus cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (Objects.isNull(eKycCreatorStatus.getEKyc())) {
            throw new BadRequestAlertException("Invalid association value provided", ENTITY_NAME, "null");
        }
        Long eKycId = eKycCreatorStatus.getEKyc().getId();
        eKycRepository.findById(eKycId).ifPresent(eKycCreatorStatus::eKyc);
        EKycCreatorStatus result = eKycCreatorStatusRepository.save(eKycCreatorStatus);
        return ResponseEntity
            .created(new URI("/api/e-kyc-creator-statuses/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /e-kyc-creator-statuses/:id} : Updates an existing eKycCreatorStatus.
     *
     * @param id the id of the eKycCreatorStatus to save.
     * @param eKycCreatorStatus the eKycCreatorStatus to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eKycCreatorStatus,
     * or with status {@code 400 (Bad Request)} if the eKycCreatorStatus is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eKycCreatorStatus couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/e-kyc-creator-statuses/{id}")
    public ResponseEntity<EKycCreatorStatus> updateEKycCreatorStatus(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody EKycCreatorStatus eKycCreatorStatus
    ) throws URISyntaxException {
        log.debug("REST request to update EKycCreatorStatus : {}, {}", id, eKycCreatorStatus);
        if (eKycCreatorStatus.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eKycCreatorStatus.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eKycCreatorStatusRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        EKycCreatorStatus result = eKycCreatorStatusRepository.save(eKycCreatorStatus);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eKycCreatorStatus.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /e-kyc-creator-statuses/:id} : Partial updates given fields of an existing eKycCreatorStatus, field will ignore if it is null
     *
     * @param id the id of the eKycCreatorStatus to save.
     * @param eKycCreatorStatus the eKycCreatorStatus to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eKycCreatorStatus,
     * or with status {@code 400 (Bad Request)} if the eKycCreatorStatus is not valid,
     * or with status {@code 404 (Not Found)} if the eKycCreatorStatus is not found,
     * or with status {@code 500 (Internal Server Error)} if the eKycCreatorStatus couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/e-kyc-creator-statuses/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<EKycCreatorStatus> partialUpdateEKycCreatorStatus(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody EKycCreatorStatus eKycCreatorStatus
    ) throws URISyntaxException {
        log.debug("REST request to partial update EKycCreatorStatus partially : {}, {}", id, eKycCreatorStatus);
        if (eKycCreatorStatus.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eKycCreatorStatus.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eKycCreatorStatusRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EKycCreatorStatus> result = eKycCreatorStatusRepository
            .findById(eKycCreatorStatus.getId())
            .map(
                existingEKycCreatorStatus -> {
                    if (eKycCreatorStatus.getStatus() != null) {
                        existingEKycCreatorStatus.setStatus(eKycCreatorStatus.getStatus());
                    }
                    if (eKycCreatorStatus.getReason() != null) {
                        existingEKycCreatorStatus.setReason(eKycCreatorStatus.getReason());
                    }
                    if (eKycCreatorStatus.getUpdatedAt() != null) {
                        existingEKycCreatorStatus.setUpdatedAt(eKycCreatorStatus.getUpdatedAt());
                    }
                    if (eKycCreatorStatus.getUpdatedBy() != null) {
                        existingEKycCreatorStatus.setUpdatedBy(eKycCreatorStatus.getUpdatedBy());
                    }
                    if (eKycCreatorStatus.getFullResult() != null) {
                        existingEKycCreatorStatus.setFullResult(eKycCreatorStatus.getFullResult());
                    }

                    return existingEKycCreatorStatus;
                }
            )
            .map(eKycCreatorStatusRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eKycCreatorStatus.getId().toString())
        );
    }

    /**
     * {@code GET  /e-kyc-creator-statuses} : get all the eKycCreatorStatuses.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eKycCreatorStatuses in body.
     */
    @GetMapping("/e-kyc-creator-statuses")
    @Transactional(readOnly = true)
    public List<EKycCreatorStatus> getAllEKycCreatorStatuses() {
        log.debug("REST request to get all EKycCreatorStatuses");
        return eKycCreatorStatusRepository.findAll();
    }

    /**
     * {@code GET  /e-kyc-creator-statuses/:id} : get the "id" eKycCreatorStatus.
     *
     * @param id the id of the eKycCreatorStatus to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eKycCreatorStatus, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/e-kyc-creator-statuses/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<EKycCreatorStatus> getEKycCreatorStatus(@PathVariable Long id) {
        log.debug("REST request to get EKycCreatorStatus : {}", id);
        Optional<EKycCreatorStatus> eKycCreatorStatus = eKycCreatorStatusRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(eKycCreatorStatus);
    }

    /**
     * {@code DELETE  /e-kyc-creator-statuses/:id} : delete the "id" eKycCreatorStatus.
     *
     * @param id the id of the eKycCreatorStatus to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/e-kyc-creator-statuses/{id}")
    public ResponseEntity<Void> deleteEKycCreatorStatus(@PathVariable Long id) {
        log.debug("REST request to delete EKycCreatorStatus : {}", id);
        eKycCreatorStatusRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
