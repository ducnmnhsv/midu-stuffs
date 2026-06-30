package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.repository.CopyPortfolioDetailsRepository;
import com.difisoft.nhsv.admin.service.CopyPortfolioDetailsQueryService;
import com.difisoft.nhsv.admin.service.CopyPortfolioDetailsService;
import com.difisoft.nhsv.admin.service.criteria.CopyPortfolioDetailsCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailsDTO;
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
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CopyPortfolioDetails}.
 */
@RestController
@RequestMapping("/api")
public class CopyPortfolioDetailsResource {

    private final Logger log = LoggerFactory.getLogger(CopyPortfolioDetailsResource.class);

    private static final String ENTITY_NAME = "copyPortfolioDetails";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CopyPortfolioDetailsService copyPortfolioDetailsService;

    private final CopyPortfolioDetailsRepository copyPortfolioDetailsRepository;

    private final CopyPortfolioDetailsQueryService copyPortfolioDetailsQueryService;

    public CopyPortfolioDetailsResource(
        CopyPortfolioDetailsService copyPortfolioDetailsService,
        CopyPortfolioDetailsRepository copyPortfolioDetailsRepository,
        CopyPortfolioDetailsQueryService copyPortfolioDetailsQueryService
    ) {
        this.copyPortfolioDetailsService = copyPortfolioDetailsService;
        this.copyPortfolioDetailsRepository = copyPortfolioDetailsRepository;
        this.copyPortfolioDetailsQueryService = copyPortfolioDetailsQueryService;
    }

    /**
     * {@code POST  /copy-portfolio-details} : Create a new copyPortfolioDetails.
     *
     * @param copyPortfolioDetailsDTO the copyPortfolioDetailsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new copyPortfolioDetailsDTO, or with status {@code 400 (Bad Request)} if the copyPortfolioDetails has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/copy-portfolio-details")
    public ResponseEntity<CopyPortfolioDetailsDTO> createCopyPortfolioDetails(
        @Valid @RequestBody CopyPortfolioDetailsDTO copyPortfolioDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to save CopyPortfolioDetails : {}", copyPortfolioDetailsDTO);
        if (copyPortfolioDetailsDTO.getId() != null) {
            throw new BadRequestAlertException("A new copyPortfolioDetails cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CopyPortfolioDetailsDTO result = copyPortfolioDetailsService.save(copyPortfolioDetailsDTO);
        return ResponseEntity
            .created(new URI("/api/copy-portfolio-details/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /copy-portfolio-details/:id} : Updates an existing copyPortfolioDetails.
     *
     * @param id the id of the copyPortfolioDetailsDTO to save.
     * @param copyPortfolioDetailsDTO the copyPortfolioDetailsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyPortfolioDetailsDTO,
     * or with status {@code 400 (Bad Request)} if the copyPortfolioDetailsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the copyPortfolioDetailsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/copy-portfolio-details/{id}")
    public ResponseEntity<CopyPortfolioDetailsDTO> updateCopyPortfolioDetails(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CopyPortfolioDetailsDTO copyPortfolioDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CopyPortfolioDetails : {}, {}", id, copyPortfolioDetailsDTO);
        if (copyPortfolioDetailsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyPortfolioDetailsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyPortfolioDetailsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CopyPortfolioDetailsDTO result = copyPortfolioDetailsService.update(copyPortfolioDetailsDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyPortfolioDetailsDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /copy-portfolio-details/:id} : Partial updates given fields of an existing copyPortfolioDetails, field will ignore if it is null
     *
     * @param id the id of the copyPortfolioDetailsDTO to save.
     * @param copyPortfolioDetailsDTO the copyPortfolioDetailsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyPortfolioDetailsDTO,
     * or with status {@code 400 (Bad Request)} if the copyPortfolioDetailsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the copyPortfolioDetailsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the copyPortfolioDetailsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/copy-portfolio-details/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CopyPortfolioDetailsDTO> partialUpdateCopyPortfolioDetails(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CopyPortfolioDetailsDTO copyPortfolioDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CopyPortfolioDetails partially : {}, {}", id, copyPortfolioDetailsDTO);
        if (copyPortfolioDetailsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyPortfolioDetailsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyPortfolioDetailsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CopyPortfolioDetailsDTO> result = copyPortfolioDetailsService.partialUpdate(copyPortfolioDetailsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyPortfolioDetailsDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /copy-portfolio-details} : get all the copyPortfolioDetails.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of copyPortfolioDetails in body.
     */
    @GetMapping("/copy-portfolio-details")
    public ResponseEntity<List<CopyPortfolioDetailsDTO>> getAllCopyPortfolioDetails(
        CopyPortfolioDetailsCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CopyPortfolioDetails by criteria: {}", criteria);
        Page<CopyPortfolioDetailsDTO> page = copyPortfolioDetailsQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /copy-portfolio-details/count} : count all the copyPortfolioDetails.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/copy-portfolio-details/count")
    public ResponseEntity<Long> countCopyPortfolioDetails(CopyPortfolioDetailsCriteria criteria) {
        log.debug("REST request to count CopyPortfolioDetails by criteria: {}", criteria);
        return ResponseEntity.ok().body(copyPortfolioDetailsQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /copy-portfolio-details/:id} : get the "id" copyPortfolioDetails.
     *
     * @param id the id of the copyPortfolioDetailsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the copyPortfolioDetailsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/copy-portfolio-details/{id}")
    public ResponseEntity<CopyPortfolioDetailsDTO> getCopyPortfolioDetails(@PathVariable Long id) {
        log.debug("REST request to get CopyPortfolioDetails : {}", id);
        Optional<CopyPortfolioDetailsDTO> copyPortfolioDetailsDTO = copyPortfolioDetailsService.findOne(id);
        return ResponseUtil.wrapOrNotFound(copyPortfolioDetailsDTO);
    }

    /**
     * {@code DELETE  /copy-portfolio-details/:id} : delete the "id" copyPortfolioDetails.
     *
     * @param id the id of the copyPortfolioDetailsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/copy-portfolio-details/{id}")
    public ResponseEntity<Void> deleteCopyPortfolioDetails(@PathVariable Long id) {
        log.debug("REST request to delete CopyPortfolioDetails : {}", id);
        copyPortfolioDetailsService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
