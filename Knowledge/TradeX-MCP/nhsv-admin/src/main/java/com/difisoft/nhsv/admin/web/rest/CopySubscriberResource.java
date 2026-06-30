package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.repository.CopySubscriberRepository;
import com.difisoft.nhsv.admin.service.CopySubscriberQueryService;
import com.difisoft.nhsv.admin.service.CopySubscriberService;
import com.difisoft.nhsv.admin.service.criteria.CopySubscriberCriteria;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberDTO;
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
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CopySubscriber}.
 */
@RestController
@RequestMapping("/api")
public class CopySubscriberResource {

    private final Logger log = LoggerFactory.getLogger(CopySubscriberResource.class);

    private static final String ENTITY_NAME = "copySubscriber";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CopySubscriberService copySubscriberService;

    private final CopySubscriberRepository copySubscriberRepository;

    private final CopySubscriberQueryService copySubscriberQueryService;

    public CopySubscriberResource(
        CopySubscriberService copySubscriberService,
        CopySubscriberRepository copySubscriberRepository,
        CopySubscriberQueryService copySubscriberQueryService
    ) {
        this.copySubscriberService = copySubscriberService;
        this.copySubscriberRepository = copySubscriberRepository;
        this.copySubscriberQueryService = copySubscriberQueryService;
    }

    /**
     * {@code POST  /copy-subscribers} : Create a new copySubscriber.
     *
     * @param copySubscriberDTO the copySubscriberDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new copySubscriberDTO, or with status {@code 400 (Bad Request)} if the copySubscriber has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/copy-subscribers")
    public ResponseEntity<CopySubscriberDTO> createCopySubscriber(@Valid @RequestBody CopySubscriberDTO copySubscriberDTO)
        throws URISyntaxException {
        log.debug("REST request to save CopySubscriber : {}", copySubscriberDTO);
        if (copySubscriberDTO.getId() != null) {
            throw new BadRequestAlertException("A new copySubscriber cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CopySubscriberDTO result = copySubscriberService.save(copySubscriberDTO);
        return ResponseEntity
            .created(new URI("/api/copy-subscribers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /copy-subscribers/:id} : Updates an existing copySubscriber.
     *
     * @param id the id of the copySubscriberDTO to save.
     * @param copySubscriberDTO the copySubscriberDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copySubscriberDTO,
     * or with status {@code 400 (Bad Request)} if the copySubscriberDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the copySubscriberDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/copy-subscribers/{id}")
    public ResponseEntity<CopySubscriberDTO> updateCopySubscriber(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CopySubscriberDTO copySubscriberDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CopySubscriber : {}, {}", id, copySubscriberDTO);
        if (copySubscriberDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copySubscriberDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copySubscriberRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CopySubscriberDTO result = copySubscriberService.update(copySubscriberDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copySubscriberDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /copy-subscribers/:id} : Partial updates given fields of an existing copySubscriber, field will ignore if it is null
     *
     * @param id the id of the copySubscriberDTO to save.
     * @param copySubscriberDTO the copySubscriberDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copySubscriberDTO,
     * or with status {@code 400 (Bad Request)} if the copySubscriberDTO is not valid,
     * or with status {@code 404 (Not Found)} if the copySubscriberDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the copySubscriberDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/copy-subscribers/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CopySubscriberDTO> partialUpdateCopySubscriber(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CopySubscriberDTO copySubscriberDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CopySubscriber partially : {}, {}", id, copySubscriberDTO);
        if (copySubscriberDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copySubscriberDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copySubscriberRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CopySubscriberDTO> result = copySubscriberService.partialUpdate(copySubscriberDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copySubscriberDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /copy-subscribers} : get all the copySubscribers.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of copySubscribers in body.
     */
    @GetMapping("/copy-subscribers")
    public ResponseEntity<List<CopySubscriberDTO>> getAllCopySubscribers(
        CopySubscriberCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CopySubscribers by criteria: {}", criteria);
        Page<CopySubscriberDTO> page = copySubscriberQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /copy-subscribers/count} : count all the copySubscribers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/copy-subscribers/count")
    public ResponseEntity<Long> countCopySubscribers(CopySubscriberCriteria criteria) {
        log.debug("REST request to count CopySubscribers by criteria: {}", criteria);
        return ResponseEntity.ok().body(copySubscriberQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /copy-subscribers/:id} : get the "id" copySubscriber.
     *
     * @param id the id of the copySubscriberDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the copySubscriberDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/copy-subscribers/{id}")
    public ResponseEntity<CopySubscriberDTO> getCopySubscriber(@PathVariable Long id) {
        log.debug("REST request to get CopySubscriber : {}", id);
        Optional<CopySubscriberDTO> copySubscriberDTO = copySubscriberService.findOne(id);
        return ResponseUtil.wrapOrNotFound(copySubscriberDTO);
    }

    /**
     * {@code DELETE  /copy-subscribers/:id} : delete the "id" copySubscriber.
     *
     * @param id the id of the copySubscriberDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/copy-subscribers/{id}")
    public ResponseEntity<Void> deleteCopySubscriber(@PathVariable Long id) {
        log.debug("REST request to delete CopySubscriber : {}", id);
        copySubscriberService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
