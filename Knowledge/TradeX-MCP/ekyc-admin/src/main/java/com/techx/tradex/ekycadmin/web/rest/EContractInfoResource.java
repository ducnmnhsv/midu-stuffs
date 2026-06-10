package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.repository.EContractInfoRepository;
import com.techx.tradex.ekycadmin.service.EContractInfoQueryService;
import com.techx.tradex.ekycadmin.service.EContractInfoService;
import com.techx.tradex.ekycadmin.service.criteria.EContractInfoCriteria;
import com.techx.tradex.ekycadmin.service.dto.EContractInfoDTO;
import com.techx.tradex.ekycadmin.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.techx.tradex.ekycadmin.domain.EContractInfo}.
 */
@RestController
@RequestMapping("/api")
public class EContractInfoResource {

    private final Logger log = LoggerFactory.getLogger(EContractInfoResource.class);

    private static final String ENTITY_NAME = "eContractInfo";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EContractInfoService eContractInfoService;

    private final EContractInfoRepository eContractInfoRepository;

    private final EContractInfoQueryService eContractInfoQueryService;

    public EContractInfoResource(
        EContractInfoService eContractInfoService,
        EContractInfoRepository eContractInfoRepository,
        EContractInfoQueryService eContractInfoQueryService
    ) {
        this.eContractInfoService = eContractInfoService;
        this.eContractInfoRepository = eContractInfoRepository;
        this.eContractInfoQueryService = eContractInfoQueryService;
    }

    /**
     * {@code POST  /e-contract-infos} : Create a new eContractInfo.
     *
     * @param eContractInfoDTO the eContractInfoDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eContractInfoDTO, or with status {@code 400 (Bad Request)} if the eContractInfo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/e-contract-infos")
    public ResponseEntity<EContractInfoDTO> createEContractInfo(@Valid @RequestBody EContractInfoDTO eContractInfoDTO)
        throws URISyntaxException {
        log.debug("REST request to save EContractInfo : {}", eContractInfoDTO);
        if (eContractInfoDTO.getId() != null) {
            throw new BadRequestAlertException("A new eContractInfo cannot already have an ID", ENTITY_NAME, "idexists");
        }
        EContractInfoDTO result = eContractInfoService.save(eContractInfoDTO);
        return ResponseEntity
            .created(new URI("/api/e-contract-infos/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /e-contract-infos/:id} : Updates an existing eContractInfo.
     *
     * @param id the id of the eContractInfoDTO to save.
     * @param eContractInfoDTO the eContractInfoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eContractInfoDTO,
     * or with status {@code 400 (Bad Request)} if the eContractInfoDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eContractInfoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/e-contract-infos/{id}")
    public ResponseEntity<EContractInfoDTO> updateEContractInfo(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EContractInfoDTO eContractInfoDTO
    ) throws URISyntaxException {
        log.debug("REST request to update EContractInfo : {}, {}", id, eContractInfoDTO);
        if (eContractInfoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eContractInfoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eContractInfoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        EContractInfoDTO result = eContractInfoService.save(eContractInfoDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eContractInfoDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /e-contract-infos/:id} : Partial updates given fields of an existing eContractInfo, field will ignore if it is null
     *
     * @param id the id of the eContractInfoDTO to save.
     * @param eContractInfoDTO the eContractInfoDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eContractInfoDTO,
     * or with status {@code 400 (Bad Request)} if the eContractInfoDTO is not valid,
     * or with status {@code 404 (Not Found)} if the eContractInfoDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the eContractInfoDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/e-contract-infos/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<EContractInfoDTO> partialUpdateEContractInfo(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EContractInfoDTO eContractInfoDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update EContractInfo partially : {}, {}", id, eContractInfoDTO);
        if (eContractInfoDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eContractInfoDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eContractInfoRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EContractInfoDTO> result = eContractInfoService.partialUpdate(eContractInfoDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eContractInfoDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /e-contract-infos} : get all the eContractInfos.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eContractInfos in body.
     */
    @GetMapping("/e-contract-infos")
    public ResponseEntity<List<EContractInfoDTO>> getAllEContractInfos(EContractInfoCriteria criteria, Pageable pageable) {
        log.debug("REST request to get EContractInfos by criteria: {}", criteria);
        Page<EContractInfoDTO> page = eContractInfoQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /e-contract-infos/count} : count all the eContractInfos.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/e-contract-infos/count")
    public ResponseEntity<Long> countEContractInfos(EContractInfoCriteria criteria) {
        log.debug("REST request to count EContractInfos by criteria: {}", criteria);
        return ResponseEntity.ok().body(eContractInfoQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /e-contract-infos/:id} : get the "id" eContractInfo.
     *
     * @param id the id of the eContractInfoDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eContractInfoDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/e-contract-infos/{id}")
    public ResponseEntity<EContractInfoDTO> getEContractInfo(@PathVariable Long id) {
        log.debug("REST request to get EContractInfo : {}", id);
        Optional<EContractInfoDTO> eContractInfoDTO = eContractInfoService.findOne(id);
        return ResponseUtil.wrapOrNotFound(eContractInfoDTO);
    }

    /**
     * {@code DELETE  /e-contract-infos/:id} : delete the "id" eContractInfo.
     *
     * @param id the id of the eContractInfoDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/e-contract-infos/{id}")
    public ResponseEntity<Void> deleteEContractInfo(@PathVariable Long id) {
        log.debug("REST request to delete EContractInfo : {}", id);
        eContractInfoService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
