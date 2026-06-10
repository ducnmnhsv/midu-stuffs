package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.domain.TtlIssuePlaceCodeMap;
import com.techx.tradex.ekycadmin.repository.TtlIssuePlaceCodeMapRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.techx.tradex.ekycadmin.domain.TtlIssuePlaceCodeMap}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class TtlIssuePlaceCodeMapResource {

    private final Logger log = LoggerFactory.getLogger(TtlIssuePlaceCodeMapResource.class);

    private static final String ENTITY_NAME = "ttlIssuePlaceCodeMap";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TtlIssuePlaceCodeMapRepository ttlIssuePlaceCodeMapRepository;

    public TtlIssuePlaceCodeMapResource(TtlIssuePlaceCodeMapRepository ttlIssuePlaceCodeMapRepository) {
        this.ttlIssuePlaceCodeMapRepository = ttlIssuePlaceCodeMapRepository;
    }

    /**
     * {@code POST  /ttl-issue-place-code-maps} : Create a new ttlIssuePlaceCodeMap.
     *
     * @param ttlIssuePlaceCodeMap the ttlIssuePlaceCodeMap to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new ttlIssuePlaceCodeMap, or with status {@code 400 (Bad Request)} if the ttlIssuePlaceCodeMap has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/ttl-issue-place-code-maps")
    public ResponseEntity<TtlIssuePlaceCodeMap> createTtlIssuePlaceCodeMap(@Valid @RequestBody TtlIssuePlaceCodeMap ttlIssuePlaceCodeMap)
        throws URISyntaxException {
        log.debug("REST request to save TtlIssuePlaceCodeMap : {}", ttlIssuePlaceCodeMap);
        if (ttlIssuePlaceCodeMap.getId() != null) {
            throw new BadRequestAlertException("A new ttlIssuePlaceCodeMap cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TtlIssuePlaceCodeMap result = ttlIssuePlaceCodeMapRepository.save(ttlIssuePlaceCodeMap);
        return ResponseEntity
            .created(new URI("/api/ttl-issue-place-code-maps/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /ttl-issue-place-code-maps/:id} : Updates an existing ttlIssuePlaceCodeMap.
     *
     * @param id the id of the ttlIssuePlaceCodeMap to save.
     * @param ttlIssuePlaceCodeMap the ttlIssuePlaceCodeMap to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated ttlIssuePlaceCodeMap,
     * or with status {@code 400 (Bad Request)} if the ttlIssuePlaceCodeMap is not valid,
     * or with status {@code 500 (Internal Server Error)} if the ttlIssuePlaceCodeMap couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/ttl-issue-place-code-maps/{id}")
    public ResponseEntity<TtlIssuePlaceCodeMap> updateTtlIssuePlaceCodeMap(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TtlIssuePlaceCodeMap ttlIssuePlaceCodeMap
    ) throws URISyntaxException {
        log.debug("REST request to update TtlIssuePlaceCodeMap : {}, {}", id, ttlIssuePlaceCodeMap);
        if (ttlIssuePlaceCodeMap.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, ttlIssuePlaceCodeMap.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!ttlIssuePlaceCodeMapRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        TtlIssuePlaceCodeMap result = ttlIssuePlaceCodeMapRepository.save(ttlIssuePlaceCodeMap);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, ttlIssuePlaceCodeMap.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /ttl-issue-place-code-maps/:id} : Partial updates given fields of an existing ttlIssuePlaceCodeMap, field will ignore if it is null
     *
     * @param id the id of the ttlIssuePlaceCodeMap to save.
     * @param ttlIssuePlaceCodeMap the ttlIssuePlaceCodeMap to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated ttlIssuePlaceCodeMap,
     * or with status {@code 400 (Bad Request)} if the ttlIssuePlaceCodeMap is not valid,
     * or with status {@code 404 (Not Found)} if the ttlIssuePlaceCodeMap is not found,
     * or with status {@code 500 (Internal Server Error)} if the ttlIssuePlaceCodeMap couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/ttl-issue-place-code-maps/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<TtlIssuePlaceCodeMap> partialUpdateTtlIssuePlaceCodeMap(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TtlIssuePlaceCodeMap ttlIssuePlaceCodeMap
    ) throws URISyntaxException {
        log.debug("REST request to partial update TtlIssuePlaceCodeMap partially : {}, {}", id, ttlIssuePlaceCodeMap);
        if (ttlIssuePlaceCodeMap.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, ttlIssuePlaceCodeMap.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!ttlIssuePlaceCodeMapRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TtlIssuePlaceCodeMap> result = ttlIssuePlaceCodeMapRepository
            .findById(ttlIssuePlaceCodeMap.getId())
            .map(
                existingTtlIssuePlaceCodeMap -> {
                    if (ttlIssuePlaceCodeMap.getCode() != null) {
                        existingTtlIssuePlaceCodeMap.setCode(ttlIssuePlaceCodeMap.getCode());
                    }
                    if (ttlIssuePlaceCodeMap.getName() != null) {
                        existingTtlIssuePlaceCodeMap.setName(ttlIssuePlaceCodeMap.getName());
                    }
                    if (ttlIssuePlaceCodeMap.getEnableRegex() != null) {
                        existingTtlIssuePlaceCodeMap.setEnableRegex(ttlIssuePlaceCodeMap.getEnableRegex());
                    }

                    return existingTtlIssuePlaceCodeMap;
                }
            )
            .map(ttlIssuePlaceCodeMapRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, ttlIssuePlaceCodeMap.getId().toString())
        );
    }

    /**
     * {@code GET  /ttl-issue-place-code-maps} : get all the ttlIssuePlaceCodeMaps.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of ttlIssuePlaceCodeMaps in body.
     */
    @GetMapping("/ttl-issue-place-code-maps")
    public ResponseEntity<List<TtlIssuePlaceCodeMap>> getAllTtlIssuePlaceCodeMaps(Pageable pageable) {
        log.debug("REST request to get a page of TtlIssuePlaceCodeMaps");
        Page<TtlIssuePlaceCodeMap> page = ttlIssuePlaceCodeMapRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /ttl-issue-place-code-maps/:id} : get the "id" ttlIssuePlaceCodeMap.
     *
     * @param id the id of the ttlIssuePlaceCodeMap to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the ttlIssuePlaceCodeMap, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ttl-issue-place-code-maps/{id}")
    public ResponseEntity<TtlIssuePlaceCodeMap> getTtlIssuePlaceCodeMap(@PathVariable Long id) {
        log.debug("REST request to get TtlIssuePlaceCodeMap : {}", id);
        Optional<TtlIssuePlaceCodeMap> ttlIssuePlaceCodeMap = ttlIssuePlaceCodeMapRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(ttlIssuePlaceCodeMap);
    }

    /**
     * {@code DELETE  /ttl-issue-place-code-maps/:id} : delete the "id" ttlIssuePlaceCodeMap.
     *
     * @param id the id of the ttlIssuePlaceCodeMap to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/ttl-issue-place-code-maps/{id}")
    public ResponseEntity<Void> deleteTtlIssuePlaceCodeMap(@PathVariable Long id) {
        log.debug("REST request to delete TtlIssuePlaceCodeMap : {}", id);
        ttlIssuePlaceCodeMapRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
