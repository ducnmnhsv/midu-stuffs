package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.domain.EKycBankList;
import com.techx.tradex.ekycadmin.repository.EKycBankListRepository;
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
 * REST controller for managing {@link com.techx.tradex.ekycadmin.domain.EKycBankList}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class EKycBankListResource {

    private final Logger log = LoggerFactory.getLogger(EKycBankListResource.class);

    private static final String ENTITY_NAME = "eKycBankList";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EKycBankListRepository eKycBankListRepository;

    public EKycBankListResource(EKycBankListRepository eKycBankListRepository) {
        this.eKycBankListRepository = eKycBankListRepository;
    }

    /**
     * {@code POST  /e-kyc-bank-lists} : Create a new eKycBankList.
     *
     * @param eKycBankList the eKycBankList to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eKycBankList, or with status {@code 400 (Bad Request)} if the eKycBankList has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/e-kyc-bank-lists")
    public ResponseEntity<EKycBankList> createEKycBankList(@RequestBody EKycBankList eKycBankList) throws URISyntaxException {
        log.debug("REST request to save EKycBankList : {}", eKycBankList);
        if (eKycBankList.getId() != null) {
            throw new BadRequestAlertException("A new eKycBankList cannot already have an ID", ENTITY_NAME, "idexists");
        }
        EKycBankList result = eKycBankListRepository.save(eKycBankList);
        return ResponseEntity
            .created(new URI("/api/e-kyc-bank-lists/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /e-kyc-bank-lists/:id} : Updates an existing eKycBankList.
     *
     * @param id the id of the eKycBankList to save.
     * @param eKycBankList the eKycBankList to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eKycBankList,
     * or with status {@code 400 (Bad Request)} if the eKycBankList is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eKycBankList couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/e-kyc-bank-lists/{id}")
    public ResponseEntity<EKycBankList> updateEKycBankList(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody EKycBankList eKycBankList
    ) throws URISyntaxException {
        log.debug("REST request to update EKycBankList : {}, {}", id, eKycBankList);
        if (eKycBankList.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eKycBankList.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eKycBankListRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        EKycBankList result = eKycBankListRepository.save(eKycBankList);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eKycBankList.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /e-kyc-bank-lists/:id} : Partial updates given fields of an existing eKycBankList, field will ignore if it is null
     *
     * @param id the id of the eKycBankList to save.
     * @param eKycBankList the eKycBankList to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eKycBankList,
     * or with status {@code 400 (Bad Request)} if the eKycBankList is not valid,
     * or with status {@code 404 (Not Found)} if the eKycBankList is not found,
     * or with status {@code 500 (Internal Server Error)} if the eKycBankList couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/e-kyc-bank-lists/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<EKycBankList> partialUpdateEKycBankList(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody EKycBankList eKycBankList
    ) throws URISyntaxException {
        log.debug("REST request to partial update EKycBankList partially : {}, {}", id, eKycBankList);
        if (eKycBankList.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eKycBankList.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eKycBankListRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EKycBankList> result = eKycBankListRepository
            .findById(eKycBankList.getId())
            .map(
                existingEKycBankList -> {
                    if (eKycBankList.getBankId() != null) {
                        existingEKycBankList.setBankId(eKycBankList.getBankId());
                    }
                    if (eKycBankList.getBankName() != null) {
                        existingEKycBankList.setBankName(eKycBankList.getBankName());
                    }
                    if (eKycBankList.getBankAccNo() != null) {
                        existingEKycBankList.setBankAccNo(eKycBankList.getBankAccNo());
                    }
                    if (eKycBankList.getOwnerName() != null) {
                        existingEKycBankList.setOwnerName(eKycBankList.getOwnerName());
                    }
                    if (eKycBankList.getBranchId() != null) {
                        existingEKycBankList.setBranchId(eKycBankList.getBranchId());
                    }

                    return existingEKycBankList;
                }
            )
            .map(eKycBankListRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eKycBankList.getId().toString())
        );
    }

    /**
     * {@code GET  /e-kyc-bank-lists} : get all the eKycBankLists.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eKycBankLists in body.
     */
    @GetMapping("/e-kyc-bank-lists")
    public List<EKycBankList> getAllEKycBankLists() {
        log.debug("REST request to get all EKycBankLists");
        return eKycBankListRepository.findAll();
    }

    /**
     * {@code GET  /e-kyc-bank-lists/:id} : get the "id" eKycBankList.
     *
     * @param id the id of the eKycBankList to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eKycBankList, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/e-kyc-bank-lists/{id}")
    public ResponseEntity<EKycBankList> getEKycBankList(@PathVariable Long id) {
        log.debug("REST request to get EKycBankList : {}", id);
        Optional<EKycBankList> eKycBankList = eKycBankListRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(eKycBankList);
    }

    /**
     * {@code DELETE  /e-kyc-bank-lists/:id} : delete the "id" eKycBankList.
     *
     * @param id the id of the eKycBankList to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/e-kyc-bank-lists/{id}")
    public ResponseEntity<Void> deleteEKycBankList(@PathVariable Long id) {
        log.debug("REST request to delete EKycBankList : {}", id);
        eKycBankListRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
