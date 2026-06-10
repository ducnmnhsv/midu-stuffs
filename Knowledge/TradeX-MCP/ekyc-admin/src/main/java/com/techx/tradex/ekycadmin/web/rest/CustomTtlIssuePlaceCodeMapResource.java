package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.domain.TtlIssuePlaceCodeMap;
import com.techx.tradex.ekycadmin.repository.TtlIssuePlaceCodeMapRepository;
import com.techx.tradex.ekycadmin.service.TtlOpenAccountService;
import com.techx.tradex.ekycadmin.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link TtlIssuePlaceCodeMap}.
 */
@RestController
@RequestMapping("/custom-api")
@Transactional
public class CustomTtlIssuePlaceCodeMapResource {
    private final TtlIssuePlaceCodeMapResource ttlIssuePlaceCodeMapResource;
    private final TtlOpenAccountService ttlOpenAccountService;

    public CustomTtlIssuePlaceCodeMapResource(
        TtlIssuePlaceCodeMapResource ttlIssuePlaceCodeMapResource,
        TtlOpenAccountService ttlOpenAccountService
    ) {
        this.ttlIssuePlaceCodeMapResource = ttlIssuePlaceCodeMapResource;
        this.ttlOpenAccountService = ttlOpenAccountService;
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
        ResponseEntity<TtlIssuePlaceCodeMap> result = this.ttlIssuePlaceCodeMapResource.createTtlIssuePlaceCodeMap(ttlIssuePlaceCodeMap);
        if (result.getStatusCode().is2xxSuccessful()) {
            ttlOpenAccountService.init();
        }
        return result;
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
        ResponseEntity<TtlIssuePlaceCodeMap> result = this.ttlIssuePlaceCodeMapResource.updateTtlIssuePlaceCodeMap(id, ttlIssuePlaceCodeMap);
        if (result.getStatusCode().is2xxSuccessful()) {
            ttlOpenAccountService.init();
        }
        return result;
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
        ResponseEntity<TtlIssuePlaceCodeMap> result = this.ttlIssuePlaceCodeMapResource.partialUpdateTtlIssuePlaceCodeMap(id, ttlIssuePlaceCodeMap);
        if (result.getStatusCode().is2xxSuccessful()) {
            ttlOpenAccountService.init();
        }
        return result;
    }

    /**
     * {@code GET  /ttl-issue-place-code-maps} : get all the ttlIssuePlaceCodeMaps.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of ttlIssuePlaceCodeMaps in body.
     */
    @GetMapping("/ttl-issue-place-code-maps")
    public ResponseEntity<List<TtlIssuePlaceCodeMap>> getAllTtlIssuePlaceCodeMaps(Pageable pageable) {
        return this.ttlIssuePlaceCodeMapResource.getAllTtlIssuePlaceCodeMaps(pageable);
    }

    /**
     * {@code GET  /ttl-issue-place-code-maps/:id} : get the "id" ttlIssuePlaceCodeMap.
     *
     * @param id the id of the ttlIssuePlaceCodeMap to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the ttlIssuePlaceCodeMap, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/ttl-issue-place-code-maps/{id}")
    public ResponseEntity<TtlIssuePlaceCodeMap> getTtlIssuePlaceCodeMap(@PathVariable Long id) {
        return this.ttlIssuePlaceCodeMapResource.getTtlIssuePlaceCodeMap(id);
    }

    /**
     * {@code DELETE  /ttl-issue-place-code-maps/:id} : delete the "id" ttlIssuePlaceCodeMap.
     *
     * @param id the id of the ttlIssuePlaceCodeMap to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/ttl-issue-place-code-maps/{id}")
    public ResponseEntity<Void> deleteTtlIssuePlaceCodeMap(@PathVariable Long id) {
        ResponseEntity<Void> result = this.ttlIssuePlaceCodeMapResource.deleteTtlIssuePlaceCodeMap(id);
        if (result.getStatusCode().is2xxSuccessful()) {
            ttlOpenAccountService.init();
        }
        return result;
    }
}
