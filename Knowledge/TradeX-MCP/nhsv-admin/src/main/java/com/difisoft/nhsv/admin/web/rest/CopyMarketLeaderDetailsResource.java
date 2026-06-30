package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.repository.CopyMarketLeaderDetailsRepository;
import com.difisoft.nhsv.admin.service.CopyMarketLeaderDetailsQueryService;
import com.difisoft.nhsv.admin.service.CopyMarketLeaderDetailsService;
import com.difisoft.nhsv.admin.service.criteria.CopyMarketLeaderDetailsCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderDetailsDTO;
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
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CopyMarketLeaderDetails}.
 */
@RestController
@RequestMapping("/api")
public class CopyMarketLeaderDetailsResource {

    private final Logger log = LoggerFactory.getLogger(CopyMarketLeaderDetailsResource.class);

    private static final String ENTITY_NAME = "copyMarketLeaderDetails";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CopyMarketLeaderDetailsService copyMarketLeaderDetailsService;

    private final CopyMarketLeaderDetailsRepository copyMarketLeaderDetailsRepository;

    private final CopyMarketLeaderDetailsQueryService copyMarketLeaderDetailsQueryService;

    public CopyMarketLeaderDetailsResource(
        CopyMarketLeaderDetailsService copyMarketLeaderDetailsService,
        CopyMarketLeaderDetailsRepository copyMarketLeaderDetailsRepository,
        CopyMarketLeaderDetailsQueryService copyMarketLeaderDetailsQueryService
    ) {
        this.copyMarketLeaderDetailsService = copyMarketLeaderDetailsService;
        this.copyMarketLeaderDetailsRepository = copyMarketLeaderDetailsRepository;
        this.copyMarketLeaderDetailsQueryService = copyMarketLeaderDetailsQueryService;
    }

    /**
     * {@code POST  /copy-market-leader-details} : Create a new copyMarketLeaderDetails.
     *
     * @param copyMarketLeaderDetailsDTO the copyMarketLeaderDetailsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new copyMarketLeaderDetailsDTO, or with status {@code 400 (Bad Request)} if the copyMarketLeaderDetails has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/copy-market-leader-details")
    public ResponseEntity<CopyMarketLeaderDetailsDTO> createCopyMarketLeaderDetails(
        @Valid @RequestBody CopyMarketLeaderDetailsDTO copyMarketLeaderDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to save CopyMarketLeaderDetails : {}", copyMarketLeaderDetailsDTO);
        if (copyMarketLeaderDetailsDTO.getId() != null) {
            throw new BadRequestAlertException("A new copyMarketLeaderDetails cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CopyMarketLeaderDetailsDTO result = copyMarketLeaderDetailsService.save(copyMarketLeaderDetailsDTO);
        return ResponseEntity
            .created(new URI("/api/copy-market-leader-details/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /copy-market-leader-details/:id} : Updates an existing copyMarketLeaderDetails.
     *
     * @param id the id of the copyMarketLeaderDetailsDTO to save.
     * @param copyMarketLeaderDetailsDTO the copyMarketLeaderDetailsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyMarketLeaderDetailsDTO,
     * or with status {@code 400 (Bad Request)} if the copyMarketLeaderDetailsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the copyMarketLeaderDetailsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/copy-market-leader-details/{id}")
    public ResponseEntity<CopyMarketLeaderDetailsDTO> updateCopyMarketLeaderDetails(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CopyMarketLeaderDetailsDTO copyMarketLeaderDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CopyMarketLeaderDetails : {}, {}", id, copyMarketLeaderDetailsDTO);
        if (copyMarketLeaderDetailsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyMarketLeaderDetailsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyMarketLeaderDetailsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CopyMarketLeaderDetailsDTO result = copyMarketLeaderDetailsService.update(copyMarketLeaderDetailsDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, copyMarketLeaderDetailsDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /copy-market-leader-details/:id} : Partial updates given fields of an existing copyMarketLeaderDetails, field will ignore if it is null
     *
     * @param id the id of the copyMarketLeaderDetailsDTO to save.
     * @param copyMarketLeaderDetailsDTO the copyMarketLeaderDetailsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyMarketLeaderDetailsDTO,
     * or with status {@code 400 (Bad Request)} if the copyMarketLeaderDetailsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the copyMarketLeaderDetailsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the copyMarketLeaderDetailsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/copy-market-leader-details/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CopyMarketLeaderDetailsDTO> partialUpdateCopyMarketLeaderDetails(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CopyMarketLeaderDetailsDTO copyMarketLeaderDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CopyMarketLeaderDetails partially : {}, {}", id, copyMarketLeaderDetailsDTO);
        if (copyMarketLeaderDetailsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyMarketLeaderDetailsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyMarketLeaderDetailsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CopyMarketLeaderDetailsDTO> result = copyMarketLeaderDetailsService.partialUpdate(copyMarketLeaderDetailsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, copyMarketLeaderDetailsDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /copy-market-leader-details} : get all the copyMarketLeaderDetails.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of copyMarketLeaderDetails in body.
     */
    @GetMapping("/copy-market-leader-details")
    public ResponseEntity<List<CopyMarketLeaderDetailsDTO>> getAllCopyMarketLeaderDetails(
        CopyMarketLeaderDetailsCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CopyMarketLeaderDetails by criteria: {}", criteria);
        Page<CopyMarketLeaderDetailsDTO> page = copyMarketLeaderDetailsQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /copy-market-leader-details/count} : count all the copyMarketLeaderDetails.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/copy-market-leader-details/count")
    public ResponseEntity<Long> countCopyMarketLeaderDetails(CopyMarketLeaderDetailsCriteria criteria) {
        log.debug("REST request to count CopyMarketLeaderDetails by criteria: {}", criteria);
        return ResponseEntity.ok().body(copyMarketLeaderDetailsQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /copy-market-leader-details/:id} : get the "id" copyMarketLeaderDetails.
     *
     * @param id the id of the copyMarketLeaderDetailsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the copyMarketLeaderDetailsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/copy-market-leader-details/{id}")
    public ResponseEntity<CopyMarketLeaderDetailsDTO> getCopyMarketLeaderDetails(@PathVariable Long id) {
        log.debug("REST request to get CopyMarketLeaderDetails : {}", id);
        Optional<CopyMarketLeaderDetailsDTO> copyMarketLeaderDetailsDTO = copyMarketLeaderDetailsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(copyMarketLeaderDetailsDTO);
    }

    /**
     * {@code DELETE  /copy-market-leader-details/:id} : delete the "id" copyMarketLeaderDetails.
     *
     * @param id the id of the copyMarketLeaderDetailsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/copy-market-leader-details/{id}")
    public ResponseEntity<Void> deleteCopyMarketLeaderDetails(@PathVariable Long id) {
        log.debug("REST request to delete CopyMarketLeaderDetails : {}", id);
        copyMarketLeaderDetailsService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
