package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.repository.CopyPortfolioHistoryRepository;
import com.difisoft.nhsv.admin.service.CopyPortfolioHistoryQueryService;
import com.difisoft.nhsv.admin.service.CopyPortfolioHistoryService;
import com.difisoft.nhsv.admin.service.criteria.CopyPortfolioHistoryCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioHistoryDTO;
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
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CopyPortfolioHistory}.
 */
@RestController
@RequestMapping("/api")
public class CopyPortfolioHistoryResource {

    private final Logger log = LoggerFactory.getLogger(CopyPortfolioHistoryResource.class);

    private static final String ENTITY_NAME = "copyPortfolioHistory";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CopyPortfolioHistoryService copyPortfolioHistoryService;

    private final CopyPortfolioHistoryRepository copyPortfolioHistoryRepository;

    private final CopyPortfolioHistoryQueryService copyPortfolioHistoryQueryService;

    public CopyPortfolioHistoryResource(
        CopyPortfolioHistoryService copyPortfolioHistoryService,
        CopyPortfolioHistoryRepository copyPortfolioHistoryRepository,
        CopyPortfolioHistoryQueryService copyPortfolioHistoryQueryService
    ) {
        this.copyPortfolioHistoryService = copyPortfolioHistoryService;
        this.copyPortfolioHistoryRepository = copyPortfolioHistoryRepository;
        this.copyPortfolioHistoryQueryService = copyPortfolioHistoryQueryService;
    }

    /**
     * {@code POST  /copy-portfolio-histories} : Create a new copyPortfolioHistory.
     *
     * @param copyPortfolioHistoryDTO the copyPortfolioHistoryDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new copyPortfolioHistoryDTO, or with status {@code 400 (Bad Request)} if the copyPortfolioHistory has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/copy-portfolio-histories")
    public ResponseEntity<CopyPortfolioHistoryDTO> createCopyPortfolioHistory(
        @Valid @RequestBody CopyPortfolioHistoryDTO copyPortfolioHistoryDTO
    ) throws URISyntaxException {
        log.debug("REST request to save CopyPortfolioHistory : {}", copyPortfolioHistoryDTO);
        if (copyPortfolioHistoryDTO.getId() != null) {
            throw new BadRequestAlertException("A new copyPortfolioHistory cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CopyPortfolioHistoryDTO result = copyPortfolioHistoryService.save(copyPortfolioHistoryDTO);
        return ResponseEntity
            .created(new URI("/api/copy-portfolio-histories/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /copy-portfolio-histories/:id} : Updates an existing copyPortfolioHistory.
     *
     * @param id the id of the copyPortfolioHistoryDTO to save.
     * @param copyPortfolioHistoryDTO the copyPortfolioHistoryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyPortfolioHistoryDTO,
     * or with status {@code 400 (Bad Request)} if the copyPortfolioHistoryDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the copyPortfolioHistoryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/copy-portfolio-histories/{id}")
    public ResponseEntity<CopyPortfolioHistoryDTO> updateCopyPortfolioHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CopyPortfolioHistoryDTO copyPortfolioHistoryDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CopyPortfolioHistory : {}, {}", id, copyPortfolioHistoryDTO);
        if (copyPortfolioHistoryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyPortfolioHistoryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyPortfolioHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CopyPortfolioHistoryDTO result = copyPortfolioHistoryService.update(copyPortfolioHistoryDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyPortfolioHistoryDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /copy-portfolio-histories/:id} : Partial updates given fields of an existing copyPortfolioHistory, field will ignore if it is null
     *
     * @param id the id of the copyPortfolioHistoryDTO to save.
     * @param copyPortfolioHistoryDTO the copyPortfolioHistoryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyPortfolioHistoryDTO,
     * or with status {@code 400 (Bad Request)} if the copyPortfolioHistoryDTO is not valid,
     * or with status {@code 404 (Not Found)} if the copyPortfolioHistoryDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the copyPortfolioHistoryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/copy-portfolio-histories/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CopyPortfolioHistoryDTO> partialUpdateCopyPortfolioHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CopyPortfolioHistoryDTO copyPortfolioHistoryDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CopyPortfolioHistory partially : {}, {}", id, copyPortfolioHistoryDTO);
        if (copyPortfolioHistoryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyPortfolioHistoryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyPortfolioHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CopyPortfolioHistoryDTO> result = copyPortfolioHistoryService.partialUpdate(copyPortfolioHistoryDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyPortfolioHistoryDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /copy-portfolio-histories} : get all the copyPortfolioHistories.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of copyPortfolioHistories in body.
     */
    @GetMapping("/copy-portfolio-histories")
    public ResponseEntity<List<CopyPortfolioHistoryDTO>> getAllCopyPortfolioHistories(
        CopyPortfolioHistoryCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CopyPortfolioHistories by criteria: {}", criteria);
        Page<CopyPortfolioHistoryDTO> page = copyPortfolioHistoryQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /copy-portfolio-histories/count} : count all the copyPortfolioHistories.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/copy-portfolio-histories/count")
    public ResponseEntity<Long> countCopyPortfolioHistories(CopyPortfolioHistoryCriteria criteria) {
        log.debug("REST request to count CopyPortfolioHistories by criteria: {}", criteria);
        return ResponseEntity.ok().body(copyPortfolioHistoryQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /copy-portfolio-histories/:id} : get the "id" copyPortfolioHistory.
     *
     * @param id the id of the copyPortfolioHistoryDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the copyPortfolioHistoryDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/copy-portfolio-histories/{id}")
    public ResponseEntity<CopyPortfolioHistoryDTO> getCopyPortfolioHistory(@PathVariable Long id) {
        log.debug("REST request to get CopyPortfolioHistory : {}", id);
        Optional<CopyPortfolioHistoryDTO> copyPortfolioHistoryDTO = copyPortfolioHistoryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(copyPortfolioHistoryDTO);
    }

    /**
     * {@code DELETE  /copy-portfolio-histories/:id} : delete the "id" copyPortfolioHistory.
     *
     * @param id the id of the copyPortfolioHistoryDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/copy-portfolio-histories/{id}")
    public ResponseEntity<Void> deleteCopyPortfolioHistory(@PathVariable Long id) {
        log.debug("REST request to delete CopyPortfolioHistory : {}", id);
        copyPortfolioHistoryService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
