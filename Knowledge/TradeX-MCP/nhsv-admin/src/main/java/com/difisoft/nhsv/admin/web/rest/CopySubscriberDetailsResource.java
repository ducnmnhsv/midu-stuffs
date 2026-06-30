package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.repository.CopySubscriberDetailsRepository;
import com.difisoft.nhsv.admin.service.CopySubscriberDetailsQueryService;
import com.difisoft.nhsv.admin.service.CopySubscriberDetailsService;
import com.difisoft.nhsv.admin.service.criteria.CopySubscriberDetailsCriteria;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberDetailsDTO;
import com.difisoft.nhsv.admin.web.rest.errors.BadRequestAlertException;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CopySubscriberDetails}.
 */
@RestController
@RequestMapping("/api")
public class CopySubscriberDetailsResource {

    private final Logger log = LoggerFactory.getLogger(CopySubscriberDetailsResource.class);

    private static final String ENTITY_NAME = "copySubscriberDetails";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CopySubscriberDetailsService copySubscriberDetailsService;

    private final CopySubscriberDetailsRepository copySubscriberDetailsRepository;

    private final CopySubscriberDetailsQueryService copySubscriberDetailsQueryService;

    public CopySubscriberDetailsResource(
        CopySubscriberDetailsService copySubscriberDetailsService,
        CopySubscriberDetailsRepository copySubscriberDetailsRepository,
        CopySubscriberDetailsQueryService copySubscriberDetailsQueryService
    ) {
        this.copySubscriberDetailsService = copySubscriberDetailsService;
        this.copySubscriberDetailsRepository = copySubscriberDetailsRepository;
        this.copySubscriberDetailsQueryService = copySubscriberDetailsQueryService;
    }

    /**
     * {@code POST  /copy-subscriber-details} : Create a new copySubscriberDetails.
     *
     * @param copySubscriberDetailsDTO the copySubscriberDetailsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new copySubscriberDetailsDTO, or with status {@code 400 (Bad Request)} if the copySubscriberDetails has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/copy-subscriber-details")
    public ResponseEntity<CopySubscriberDetailsDTO> createCopySubscriberDetails(
        @Valid @RequestBody CopySubscriberDetailsDTO copySubscriberDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to save CopySubscriberDetails : {}", copySubscriberDetailsDTO);
        if (copySubscriberDetailsDTO.getId() != null) {
            throw new BadRequestAlertException("A new copySubscriberDetails cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CopySubscriberDetailsDTO result = copySubscriberDetailsService.save(copySubscriberDetailsDTO);
        return ResponseEntity
            .created(new URI("/api/copy-subscriber-details/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /copy-subscriber-details/:id} : Updates an existing copySubscriberDetails.
     *
     * @param id the id of the copySubscriberDetailsDTO to save.
     * @param copySubscriberDetailsDTO the copySubscriberDetailsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copySubscriberDetailsDTO,
     * or with status {@code 400 (Bad Request)} if the copySubscriberDetailsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the copySubscriberDetailsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/copy-subscriber-details/{id}")
    public ResponseEntity<CopySubscriberDetailsDTO> updateCopySubscriberDetails(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CopySubscriberDetailsDTO copySubscriberDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CopySubscriberDetails : {}, {}", id, copySubscriberDetailsDTO);
        if (copySubscriberDetailsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copySubscriberDetailsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copySubscriberDetailsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CopySubscriberDetailsDTO result = copySubscriberDetailsService.update(copySubscriberDetailsDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copySubscriberDetailsDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /copy-subscriber-details/:id} : Partial updates given fields of an existing copySubscriberDetails, field will ignore if it is null
     *
     * @param id the id of the copySubscriberDetailsDTO to save.
     * @param copySubscriberDetailsDTO the copySubscriberDetailsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copySubscriberDetailsDTO,
     * or with status {@code 400 (Bad Request)} if the copySubscriberDetailsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the copySubscriberDetailsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the copySubscriberDetailsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/copy-subscriber-details/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CopySubscriberDetailsDTO> partialUpdateCopySubscriberDetails(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CopySubscriberDetailsDTO copySubscriberDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CopySubscriberDetails partially : {}, {}", id, copySubscriberDetailsDTO);
        if (copySubscriberDetailsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copySubscriberDetailsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copySubscriberDetailsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CopySubscriberDetailsDTO> result = copySubscriberDetailsService.partialUpdate(copySubscriberDetailsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copySubscriberDetailsDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /copy-subscriber-details} : get all the copySubscriberDetails.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of copySubscriberDetails in body.
     */
    @GetMapping("/copy-subscriber-details")
    public ResponseEntity<List<CopySubscriberDetailsDTO>> getAllCopySubscriberDetails(
        CopySubscriberDetailsCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CopySubscriberDetails by criteria: {}", criteria);
        Page<CopySubscriberDetailsDTO> page = copySubscriberDetailsQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /copy-subscriber-details/count} : count all the copySubscriberDetails.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/copy-subscriber-details/count")
    public ResponseEntity<Long> countCopySubscriberDetails(CopySubscriberDetailsCriteria criteria) {
        log.debug("REST request to count CopySubscriberDetails by criteria: {}", criteria);
        return ResponseEntity.ok().body(copySubscriberDetailsQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /copy-subscriber-details/:id} : get the "id" copySubscriberDetails.
     *
     * @param id the id of the copySubscriberDetailsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the copySubscriberDetailsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/copy-subscriber-details/{id}")
    public ResponseEntity<CopySubscriberDetailsDTO> getCopySubscriberDetails(@PathVariable Long id) {
        log.debug("REST request to get CopySubscriberDetails : {}", id);
        Optional<CopySubscriberDetailsDTO> copySubscriberDetailsDTO = copySubscriberDetailsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(copySubscriberDetailsDTO);
    }

    /**
     * {@code DELETE  /copy-subscriber-details/:id} : delete the "id" copySubscriberDetails.
     *
     * @param id the id of the copySubscriberDetailsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/copy-subscriber-details/{id}")
    public ResponseEntity<Void> deleteCopySubscriberDetails(@PathVariable Long id) {
        log.debug("REST request to delete CopySubscriberDetails : {}", id);
        copySubscriberDetailsService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
