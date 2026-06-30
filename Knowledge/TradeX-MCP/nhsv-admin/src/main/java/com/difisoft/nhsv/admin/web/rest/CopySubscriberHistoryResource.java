package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.repository.CopySubscriberHistoryRepository;
import com.difisoft.nhsv.admin.service.CopySubscriberHistoryQueryService;
import com.difisoft.nhsv.admin.service.CopySubscriberHistoryService;
import com.difisoft.nhsv.admin.service.criteria.CopySubscriberHistoryCriteria;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberHistoryDTO;
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
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CopySubscriberHistory}.
 */
@RestController
@RequestMapping("/api")
public class CopySubscriberHistoryResource {

    private final Logger log = LoggerFactory.getLogger(CopySubscriberHistoryResource.class);

    private static final String ENTITY_NAME = "copySubscriberHistory";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CopySubscriberHistoryService copySubscriberHistoryService;

    private final CopySubscriberHistoryRepository copySubscriberHistoryRepository;

    private final CopySubscriberHistoryQueryService copySubscriberHistoryQueryService;

    public CopySubscriberHistoryResource(
        CopySubscriberHistoryService copySubscriberHistoryService,
        CopySubscriberHistoryRepository copySubscriberHistoryRepository,
        CopySubscriberHistoryQueryService copySubscriberHistoryQueryService
    ) {
        this.copySubscriberHistoryService = copySubscriberHistoryService;
        this.copySubscriberHistoryRepository = copySubscriberHistoryRepository;
        this.copySubscriberHistoryQueryService = copySubscriberHistoryQueryService;
    }

    /**
     * {@code POST  /copy-subscriber-histories} : Create a new copySubscriberHistory.
     *
     * @param copySubscriberHistoryDTO the copySubscriberHistoryDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new copySubscriberHistoryDTO, or with status {@code 400 (Bad Request)} if the copySubscriberHistory has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/copy-subscriber-histories")
    public ResponseEntity<CopySubscriberHistoryDTO> createCopySubscriberHistory(
        @Valid @RequestBody CopySubscriberHistoryDTO copySubscriberHistoryDTO
    ) throws URISyntaxException {
        log.debug("REST request to save CopySubscriberHistory : {}", copySubscriberHistoryDTO);
        if (copySubscriberHistoryDTO.getId() != null) {
            throw new BadRequestAlertException("A new copySubscriberHistory cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CopySubscriberHistoryDTO result = copySubscriberHistoryService.save(copySubscriberHistoryDTO);
        return ResponseEntity
            .created(new URI("/api/copy-subscriber-histories/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /copy-subscriber-histories/:id} : Updates an existing copySubscriberHistory.
     *
     * @param id the id of the copySubscriberHistoryDTO to save.
     * @param copySubscriberHistoryDTO the copySubscriberHistoryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copySubscriberHistoryDTO,
     * or with status {@code 400 (Bad Request)} if the copySubscriberHistoryDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the copySubscriberHistoryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/copy-subscriber-histories/{id}")
    public ResponseEntity<CopySubscriberHistoryDTO> updateCopySubscriberHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CopySubscriberHistoryDTO copySubscriberHistoryDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CopySubscriberHistory : {}, {}", id, copySubscriberHistoryDTO);
        if (copySubscriberHistoryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copySubscriberHistoryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copySubscriberHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CopySubscriberHistoryDTO result = copySubscriberHistoryService.update(copySubscriberHistoryDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copySubscriberHistoryDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /copy-subscriber-histories/:id} : Partial updates given fields of an existing copySubscriberHistory, field will ignore if it is null
     *
     * @param id the id of the copySubscriberHistoryDTO to save.
     * @param copySubscriberHistoryDTO the copySubscriberHistoryDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copySubscriberHistoryDTO,
     * or with status {@code 400 (Bad Request)} if the copySubscriberHistoryDTO is not valid,
     * or with status {@code 404 (Not Found)} if the copySubscriberHistoryDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the copySubscriberHistoryDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/copy-subscriber-histories/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CopySubscriberHistoryDTO> partialUpdateCopySubscriberHistory(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CopySubscriberHistoryDTO copySubscriberHistoryDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CopySubscriberHistory partially : {}, {}", id, copySubscriberHistoryDTO);
        if (copySubscriberHistoryDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copySubscriberHistoryDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copySubscriberHistoryRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CopySubscriberHistoryDTO> result = copySubscriberHistoryService.partialUpdate(copySubscriberHistoryDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copySubscriberHistoryDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /copy-subscriber-histories} : get all the copySubscriberHistories.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of copySubscriberHistories in body.
     */
    @GetMapping("/copy-subscriber-histories")
    public ResponseEntity<List<CopySubscriberHistoryDTO>> getAllCopySubscriberHistories(
        CopySubscriberHistoryCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CopySubscriberHistories by criteria: {}", criteria);
        Page<CopySubscriberHistoryDTO> page = copySubscriberHistoryQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /copy-subscriber-histories/count} : count all the copySubscriberHistories.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/copy-subscriber-histories/count")
    public ResponseEntity<Long> countCopySubscriberHistories(CopySubscriberHistoryCriteria criteria) {
        log.debug("REST request to count CopySubscriberHistories by criteria: {}", criteria);
        return ResponseEntity.ok().body(copySubscriberHistoryQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /copy-subscriber-histories/:id} : get the "id" copySubscriberHistory.
     *
     * @param id the id of the copySubscriberHistoryDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the copySubscriberHistoryDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/copy-subscriber-histories/{id}")
    public ResponseEntity<CopySubscriberHistoryDTO> getCopySubscriberHistory(@PathVariable Long id) {
        log.debug("REST request to get CopySubscriberHistory : {}", id);
        Optional<CopySubscriberHistoryDTO> copySubscriberHistoryDTO = copySubscriberHistoryService.findOne(id);
        return ResponseUtil.wrapOrNotFound(copySubscriberHistoryDTO);
    }

    /**
     * {@code DELETE  /copy-subscriber-histories/:id} : delete the "id" copySubscriberHistory.
     *
     * @param id the id of the copySubscriberHistoryDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/copy-subscriber-histories/{id}")
    public ResponseEntity<Void> deleteCopySubscriberHistory(@PathVariable Long id) {
        log.debug("REST request to delete CopySubscriberHistory : {}", id);
        copySubscriberHistoryService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
