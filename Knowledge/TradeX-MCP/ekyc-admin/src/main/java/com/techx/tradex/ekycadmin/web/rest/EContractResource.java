package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.repository.EContractRepository;
import com.techx.tradex.ekycadmin.service.EContractQueryService;
import com.techx.tradex.ekycadmin.service.EContractService;
import com.techx.tradex.ekycadmin.service.criteria.EContractCriteria;
import com.techx.tradex.ekycadmin.service.dto.EContractDTO;
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
 * REST controller for managing {@link com.techx.tradex.ekycadmin.domain.EContract}.
 */
@RestController
@RequestMapping("/api")
public class EContractResource {

    private final Logger log = LoggerFactory.getLogger(EContractResource.class);

    private static final String ENTITY_NAME = "eContract";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final EContractService eContractService;

    private final EContractRepository eContractRepository;

    private final EContractQueryService eContractQueryService;

    public EContractResource(
        EContractService eContractService,
        EContractRepository eContractRepository,
        EContractQueryService eContractQueryService
    ) {
        this.eContractService = eContractService;
        this.eContractRepository = eContractRepository;
        this.eContractQueryService = eContractQueryService;
    }

    /**
     * {@code POST  /e-contracts} : Create a new eContract.
     *
     * @param eContractDTO the eContractDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new eContractDTO, or with status {@code 400 (Bad Request)} if the eContract has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/e-contracts")
    public ResponseEntity<EContractDTO> createEContract(@Valid @RequestBody EContractDTO eContractDTO) throws URISyntaxException {
        log.debug("REST request to save EContract : {}", eContractDTO);
        if (eContractDTO.getId() != null) {
            throw new BadRequestAlertException("A new eContract cannot already have an ID", ENTITY_NAME, "idexists");
        }
        EContractDTO result = eContractService.save(eContractDTO);
        return ResponseEntity
            .created(new URI("/api/e-contracts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /e-contracts/:id} : Updates an existing eContract.
     *
     * @param id the id of the eContractDTO to save.
     * @param eContractDTO the eContractDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eContractDTO,
     * or with status {@code 400 (Bad Request)} if the eContractDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the eContractDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/e-contracts/{id}")
    public ResponseEntity<EContractDTO> updateEContract(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody EContractDTO eContractDTO
    ) throws URISyntaxException {
        log.debug("REST request to update EContract : {}, {}", id, eContractDTO);
        if (eContractDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eContractDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eContractRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        EContractDTO result = eContractService.save(eContractDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eContractDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /e-contracts/:id} : Partial updates given fields of an existing eContract, field will ignore if it is null
     *
     * @param id the id of the eContractDTO to save.
     * @param eContractDTO the eContractDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated eContractDTO,
     * or with status {@code 400 (Bad Request)} if the eContractDTO is not valid,
     * or with status {@code 404 (Not Found)} if the eContractDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the eContractDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/e-contracts/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<EContractDTO> partialUpdateEContract(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody EContractDTO eContractDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update EContract partially : {}, {}", id, eContractDTO);
        if (eContractDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, eContractDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!eContractRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<EContractDTO> result = eContractService.partialUpdate(eContractDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, eContractDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /e-contracts} : get all the eContracts.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of eContracts in body.
     */
    @GetMapping("/e-contracts")
    public ResponseEntity<List<EContractDTO>> getAllEContracts(EContractCriteria criteria, Pageable pageable) {
        log.debug("REST request to get EContracts by criteria: {}", criteria);
        Page<EContractDTO> page = eContractQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /e-contracts/count} : count all the eContracts.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/e-contracts/count")
    public ResponseEntity<Long> countEContracts(EContractCriteria criteria) {
        log.debug("REST request to count EContracts by criteria: {}", criteria);
        return ResponseEntity.ok().body(eContractQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /e-contracts/:id} : get the "id" eContract.
     *
     * @param id the id of the eContractDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the eContractDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/e-contracts/{id}")
    public ResponseEntity<EContractDTO> getEContract(@PathVariable Long id) {
        log.debug("REST request to get EContract : {}", id);
        Optional<EContractDTO> eContractDTO = eContractService.findOne(id);
        return ResponseUtil.wrapOrNotFound(eContractDTO);
    }

    /**
     * {@code DELETE  /e-contracts/:id} : delete the "id" eContract.
     *
     * @param id the id of the eContractDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/e-contracts/{id}")
    public ResponseEntity<Void> deleteEContract(@PathVariable Long id) {
        log.debug("REST request to delete EContract : {}", id);
        eContractService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
