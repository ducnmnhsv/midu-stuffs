package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.repository.CopyPortfolioDetailHistoryRepository;
import com.difisoft.nhsv.admin.service.CopyPortfolioDetailHistoryQueryService;
import com.difisoft.nhsv.admin.service.CopyPortfolioDetailHistoryService;
import com.difisoft.nhsv.admin.service.criteria.CopyPortfolioDetailHistoryCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailHistoryDTO;
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
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CopyPortfolioDetailHistory}.
 */
@RestController
@RequestMapping("/api")
public class CopyPortfolioDetailHistoryResource {

    private final Logger log = LoggerFactory.getLogger(CopyPortfolioDetailHistoryResource.class);

    private static final String ENTITY_NAME = "copyPortfolioDetailHistory";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CopyPortfolioDetailHistoryService copyPortfolioDetailHistoryService;

    private final CopyPortfolioDetailHistoryRepository copyPortfolioDetailHistoryRepository;

    private final CopyPortfolioDetailHistoryQueryService copyPortfolioDetailHistoryQueryService;

    public CopyPortfolioDetailHistoryResource(
        CopyPortfolioDetailHistoryService copyPortfolioDetailHistoryService,
        CopyPortfolioDetailHistoryRepository copyPortfolioDetailHistoryRepository,
        CopyPortfolioDetailHistoryQueryService copyPortfolioDetailHistoryQueryService
    ) {
        this.copyPortfolioDetailHistoryService = copyPortfolioDetailHistoryService;
        this.copyPortfolioDetailHistoryRepository = copyPortfolioDetailHistoryRepository;
        this.copyPortfolioDetailHistoryQueryService = copyPortfolioDetailHistoryQueryService;
    }

    /**
     * {@code POST  /copy-portfolio-detail-histories} : Create a new copyPortfolioDetailHistory.
     *
     * @param copyPortfolioDetailHistoryDTO the copyPortfolioDetailHistoryDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new copyPortfolioDetailHistoryDTO, or with status {@code 400 (Bad Request)} if the copyPortfolioDetailHistory has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/copy-portfolio-detail-histories")
    public ResponseEntity<CopyPortfolioDetailHistoryDTO> createCopyPortfolioDetailHistory(
        @Valid @RequestBody CopyPortfolioDetailHistoryDTO copyPortfolioDetailHistoryDTO
    ) throws URISyntaxException {
        log.debug("REST request to save CopyPortfolioDetailHistory : {}", copyPortfolioDetailHistoryDTO);
        if (copyPortfolioDetailHistoryDTO.getId() != null) {
            throw new BadRequestAlertException("A new copyPortfolioDetailHistory cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CopyPortfolioDetailHistoryDTO result = copyPortfolioDetailHistoryService.save(copyPortfolioDetailHistoryDTO);
        return ResponseEntity
            .created(new URI("/api/copy-portfolio-detail-histories/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /copy-portfolio-detail-histories/:id} : Updates an existing copyPortfolioDetailHistory.
     *
     * @param id the id of the copyPortfolioDetailHistoryDTO to save.
     * @param copyPortfolioDetailHistoryDTO the copyPortfolioDetailHistoryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyPortfolioDetailHistoryDTO,
     * or with status {@code 400 (Bad Request)} if the copyPortfolioDetailHistoryDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the copyPortfolioDetailHistoryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/copy-portfolio-detail-histories/{id}")
    public ResponseEntity<CopyPortfolioDetailHistoryDTO> updateCopyPortfolioDetailHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CopyPortfolioDetailHistoryDTO copyPortfolioDetailHistoryDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CopyPortfolioDetailHistory : {}, {}", id, copyPortfolioDetailHistoryDTO);
        if (copyPortfolioDetailHistoryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyPortfolioDetailHistoryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyPortfolioDetailHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CopyPortfolioDetailHistoryDTO result = copyPortfolioDetailHistoryService.update(copyPortfolioDetailHistoryDTO);
        return ResponseEntity
            .ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyPortfolioDetailHistoryDTO.getId().toString())
            )
            .body(result);
    }

    /**
     * {@code PATCH  /copy-portfolio-detail-histories/:id} : Partial updates given fields of an existing copyPortfolioDetailHistory, field will ignore if it is null
     *
     * @param id the id of the copyPortfolioDetailHistoryDTO to save.
     * @param copyPortfolioDetailHistoryDTO the copyPortfolioDetailHistoryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyPortfolioDetailHistoryDTO,
     * or with status {@code 400 (Bad Request)} if the copyPortfolioDetailHistoryDTO is not valid,
     * or with status {@code 404 (Not Found)} if the copyPortfolioDetailHistoryDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the copyPortfolioDetailHistoryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/copy-portfolio-detail-histories/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CopyPortfolioDetailHistoryDTO> partialUpdateCopyPortfolioDetailHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CopyPortfolioDetailHistoryDTO copyPortfolioDetailHistoryDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CopyPortfolioDetailHistory partially : {}, {}", id, copyPortfolioDetailHistoryDTO);
        if (copyPortfolioDetailHistoryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyPortfolioDetailHistoryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyPortfolioDetailHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CopyPortfolioDetailHistoryDTO> result = copyPortfolioDetailHistoryService.partialUpdate(copyPortfolioDetailHistoryDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyPortfolioDetailHistoryDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /copy-portfolio-detail-histories} : get all the copyPortfolioDetailHistories.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of copyPortfolioDetailHistories in body.
     */
    @GetMapping("/copy-portfolio-detail-histories")
    public ResponseEntity<List<CopyPortfolioDetailHistoryDTO>> getAllCopyPortfolioDetailHistories(
        CopyPortfolioDetailHistoryCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CopyPortfolioDetailHistories by criteria: {}", criteria);
        Page<CopyPortfolioDetailHistoryDTO> page = copyPortfolioDetailHistoryQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /copy-portfolio-detail-histories/count} : count all the copyPortfolioDetailHistories.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/copy-portfolio-detail-histories/count")
    public ResponseEntity<Long> countCopyPortfolioDetailHistories(CopyPortfolioDetailHistoryCriteria criteria) {
        log.debug("REST request to count CopyPortfolioDetailHistories by criteria: {}", criteria);
        return ResponseEntity.ok().body(copyPortfolioDetailHistoryQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /copy-portfolio-detail-histories/:id} : get the "id" copyPortfolioDetailHistory.
     *
     * @param id the id of the copyPortfolioDetailHistoryDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the copyPortfolioDetailHistoryDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/copy-portfolio-detail-histories/{id}")
    public ResponseEntity<CopyPortfolioDetailHistoryDTO> getCopyPortfolioDetailHistory(@PathVariable Long id) {
        log.debug("REST request to get CopyPortfolioDetailHistory : {}", id);
        Optional<CopyPortfolioDetailHistoryDTO> copyPortfolioDetailHistoryDTO = copyPortfolioDetailHistoryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(copyPortfolioDetailHistoryDTO);
    }

    /**
     * {@code DELETE  /copy-portfolio-detail-histories/:id} : delete the "id" copyPortfolioDetailHistory.
     *
     * @param id the id of the copyPortfolioDetailHistoryDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/copy-portfolio-detail-histories/{id}")
    public ResponseEntity<Void> deleteCopyPortfolioDetailHistory(@PathVariable Long id) {
        log.debug("REST request to delete CopyPortfolioDetailHistory : {}", id);
        copyPortfolioDetailHistoryService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
