package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.domain.EKycAdditionalInfo;
import com.techx.tradex.ekycadmin.repository.EKycAdditionalInfoRepository;
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
 * REST controller for managing {@link com.techx.tradex.ekycadmin.domain.EKycAdditionalInfo}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class EKycAdditionalInfoResource {

    private final Logger log = LoggerFactory.getLogger(EKycAdditionalInfoResource.class);

    private static final String ENTITY_NAME = "eKycAdditionalInfo";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EKycAdditionalInfoRepository eKycAdditionalInfoRepository;

    private final EKycRepository eKycRepository;

    public EKycAdditionalInfoResource(EKycAdditionalInfoRepository eKycAdditionalInfoRepository, EKycRepository eKycRepository) {
        this.eKycAdditionalInfoRepository = eKycAdditionalInfoRepository;
        this.eKycRepository = eKycRepository;
    }

    /**
     * {@code POST  /e-kyc-additional-infos} : Create a new eKycAdditionalInfo.
     *
     * @param eKycAdditionalInfo the eKycAdditionalInfo to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eKycAdditionalInfo, or with status {@code 400 (Bad Request)} if the eKycAdditionalInfo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/e-kyc-additional-infos")
    public ResponseEntity<EKycAdditionalInfo> createEKycAdditionalInfo(@RequestBody EKycAdditionalInfo eKycAdditionalInfo)
        throws URISyntaxException {
        log.debug("REST request to save EKycAdditionalInfo : {}", eKycAdditionalInfo);
        if (eKycAdditionalInfo.getId() != null) {
            throw new BadRequestAlertException("A new eKycAdditionalInfo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        if (Objects.isNull(eKycAdditionalInfo.getEKyc())) {
            throw new BadRequestAlertException("Invalid association value provided", ENTITY_NAME, "null");
        }
        Long eKycId = eKycAdditionalInfo.getEKyc().getId();
        eKycRepository.findById(eKycId).ifPresent(eKycAdditionalInfo::eKyc);
        EKycAdditionalInfo result = eKycAdditionalInfoRepository.save(eKycAdditionalInfo);
        return ResponseEntity
            .created(new URI("/api/e-kyc-additional-infos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /e-kyc-additional-infos/:id} : Updates an existing eKycAdditionalInfo.
     *
     * @param id the id of the eKycAdditionalInfo to save.
     * @param eKycAdditionalInfo the eKycAdditionalInfo to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eKycAdditionalInfo,
     * or with status {@code 400 (Bad Request)} if the eKycAdditionalInfo is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eKycAdditionalInfo couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/e-kyc-additional-infos/{id}")
    public ResponseEntity<EKycAdditionalInfo> updateEKycAdditionalInfo(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody EKycAdditionalInfo eKycAdditionalInfo
    ) throws URISyntaxException {
        log.debug("REST request to update EKycAdditionalInfo : {}, {}", id, eKycAdditionalInfo);
        if (eKycAdditionalInfo.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eKycAdditionalInfo.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eKycAdditionalInfoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        EKycAdditionalInfo result = eKycAdditionalInfoRepository.save(eKycAdditionalInfo);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eKycAdditionalInfo.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /e-kyc-additional-infos/:id} : Partial updates given fields of an existing eKycAdditionalInfo, field will ignore if it is null
     *
     * @param id the id of the eKycAdditionalInfo to save.
     * @param eKycAdditionalInfo the eKycAdditionalInfo to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eKycAdditionalInfo,
     * or with status {@code 400 (Bad Request)} if the eKycAdditionalInfo is not valid,
     * or with status {@code 404 (Not Found)} if the eKycAdditionalInfo is not found,
     * or with status {@code 500 (Internal Server Error)} if the eKycAdditionalInfo couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/e-kyc-additional-infos/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<EKycAdditionalInfo> partialUpdateEKycAdditionalInfo(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody EKycAdditionalInfo eKycAdditionalInfo
    ) throws URISyntaxException {
        log.debug("REST request to partial update EKycAdditionalInfo partially : {}, {}", id, eKycAdditionalInfo);
        if (eKycAdditionalInfo.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eKycAdditionalInfo.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eKycAdditionalInfoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EKycAdditionalInfo> result = eKycAdditionalInfoRepository
            .findById(eKycAdditionalInfo.getId())
            .map(
                existingEKycAdditionalInfo -> {
                    if (eKycAdditionalInfo.getFullName() != null) {
                        existingEKycAdditionalInfo.setFullName(eKycAdditionalInfo.getFullName());
                    }
                    if (eKycAdditionalInfo.getBirthDay() != null) {
                        existingEKycAdditionalInfo.setBirthDay(eKycAdditionalInfo.getBirthDay());
                    }
                    if (eKycAdditionalInfo.getNationality() != null) {
                        existingEKycAdditionalInfo.setNationality(eKycAdditionalInfo.getNationality());
                    }
                    if (eKycAdditionalInfo.getIdentifierId() != null) {
                        existingEKycAdditionalInfo.setIdentifierId(eKycAdditionalInfo.getIdentifierId());
                    }
                    if (eKycAdditionalInfo.getIssueDate() != null) {
                        existingEKycAdditionalInfo.setIssueDate(eKycAdditionalInfo.getIssueDate());
                    }
                    if (eKycAdditionalInfo.getIssuePlace() != null) {
                        existingEKycAdditionalInfo.setIssuePlace(eKycAdditionalInfo.getIssuePlace());
                    }
                    if (eKycAdditionalInfo.getPermanentAddress() != null) {
                        existingEKycAdditionalInfo.setPermanentAddress(eKycAdditionalInfo.getPermanentAddress());
                    }
                    if (eKycAdditionalInfo.getContactAddress() != null) {
                        existingEKycAdditionalInfo.setContactAddress(eKycAdditionalInfo.getContactAddress());
                    }
                    if (eKycAdditionalInfo.getOccupation() != null) {
                        existingEKycAdditionalInfo.setOccupation(eKycAdditionalInfo.getOccupation());
                    }
                    if (eKycAdditionalInfo.getPosition() != null) {
                        existingEKycAdditionalInfo.setPosition(eKycAdditionalInfo.getPosition());
                    }
                    if (eKycAdditionalInfo.getPhoneNumber() != null) {
                        existingEKycAdditionalInfo.setPhoneNumber(eKycAdditionalInfo.getPhoneNumber());
                    }
                    if (eKycAdditionalInfo.getVisaNo() != null) {
                        existingEKycAdditionalInfo.setVisaNo(eKycAdditionalInfo.getVisaNo());
                    }
                    if (eKycAdditionalInfo.getVisaIssuePlace() != null) {
                        existingEKycAdditionalInfo.setVisaIssuePlace(eKycAdditionalInfo.getVisaIssuePlace());
                    }
                    if (eKycAdditionalInfo.getForeignResidence() != null) {
                        existingEKycAdditionalInfo.setForeignResidence(eKycAdditionalInfo.getForeignResidence());
                    }
                    if (eKycAdditionalInfo.getInvestmentGoal() != null) {
                        existingEKycAdditionalInfo.setInvestmentGoal(eKycAdditionalInfo.getInvestmentGoal());
                    }
                    if (eKycAdditionalInfo.getRisk() != null) {
                        existingEKycAdditionalInfo.setRisk(eKycAdditionalInfo.getRisk());
                    }
                    if (eKycAdditionalInfo.getExperienced() != null) {
                        existingEKycAdditionalInfo.setExperienced(eKycAdditionalInfo.getExperienced());
                    }

                    return existingEKycAdditionalInfo;
                }
            )
            .map(eKycAdditionalInfoRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eKycAdditionalInfo.getId().toString())
        );
    }

    /**
     * {@code GET  /e-kyc-additional-infos} : get all the eKycAdditionalInfos.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eKycAdditionalInfos in body.
     */
    @GetMapping("/e-kyc-additional-infos")
    @Transactional(readOnly = true)
    public List<EKycAdditionalInfo> getAllEKycAdditionalInfos() {
        log.debug("REST request to get all EKycAdditionalInfos");
        return eKycAdditionalInfoRepository.findAll();
    }

    /**
     * {@code GET  /e-kyc-additional-infos/:id} : get the "id" eKycAdditionalInfo.
     *
     * @param id the id of the eKycAdditionalInfo to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eKycAdditionalInfo, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/e-kyc-additional-infos/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<EKycAdditionalInfo> getEKycAdditionalInfo(@PathVariable Long id) {
        log.debug("REST request to get EKycAdditionalInfo : {}", id);
        Optional<EKycAdditionalInfo> eKycAdditionalInfo = eKycAdditionalInfoRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(eKycAdditionalInfo);
    }

    /**
     * {@code DELETE  /e-kyc-additional-infos/:id} : delete the "id" eKycAdditionalInfo.
     *
     * @param id the id of the eKycAdditionalInfo to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/e-kyc-additional-infos/{id}")
    public ResponseEntity<Void> deleteEKycAdditionalInfo(@PathVariable Long id) {
        log.debug("REST request to delete EKycAdditionalInfo : {}", id);
        eKycAdditionalInfoRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
