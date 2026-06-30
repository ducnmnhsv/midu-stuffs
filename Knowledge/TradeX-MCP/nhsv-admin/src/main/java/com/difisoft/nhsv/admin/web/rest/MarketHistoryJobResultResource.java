package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.domain.MarketHistoryJobResult;
import com.difisoft.nhsv.admin.repository.MarketHistoryJobResultRepository;
import com.difisoft.nhsv.admin.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.MarketHistoryJobResult}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class MarketHistoryJobResultResource {

    private final Logger log = LoggerFactory.getLogger(MarketHistoryJobResultResource.class);

    private static final String ENTITY_NAME = "marketHistoryJobResult";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MarketHistoryJobResultRepository marketHistoryJobResultRepository;

    public MarketHistoryJobResultResource(MarketHistoryJobResultRepository marketHistoryJobResultRepository) {
        this.marketHistoryJobResultRepository = marketHistoryJobResultRepository;
    }

    /**
     * {@code POST  /market-history-job-results} : Create a new marketHistoryJobResult.
     *
     * @param marketHistoryJobResult the marketHistoryJobResult to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new marketHistoryJobResult, or with status {@code 400 (Bad Request)} if the marketHistoryJobResult has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/market-history-job-results")
    public ResponseEntity<MarketHistoryJobResult> createMarketHistoryJobResult(@RequestBody MarketHistoryJobResult marketHistoryJobResult)
        throws URISyntaxException {
        log.debug("REST request to save MarketHistoryJobResult : {}", marketHistoryJobResult);
        if (marketHistoryJobResult.getId() != null) {
            throw new BadRequestAlertException("A new marketHistoryJobResult cannot already have an ID", ENTITY_NAME, "idexists");
        }
        MarketHistoryJobResult result = marketHistoryJobResultRepository.save(marketHistoryJobResult);
        return ResponseEntity
            .created(new URI("/api/market-history-job-results/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /market-history-job-results/:id} : Updates an existing marketHistoryJobResult.
     *
     * @param id the id of the marketHistoryJobResult to save.
     * @param marketHistoryJobResult the marketHistoryJobResult to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated marketHistoryJobResult,
     * or with status {@code 400 (Bad Request)} if the marketHistoryJobResult is not valid,
     * or with status {@code 500 (Internal Server Error)} if the marketHistoryJobResult couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/market-history-job-results/{id}")
    public ResponseEntity<MarketHistoryJobResult> updateMarketHistoryJobResult(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MarketHistoryJobResult marketHistoryJobResult
    ) throws URISyntaxException {
        log.debug("REST request to update MarketHistoryJobResult : {}, {}", id, marketHistoryJobResult);
        if (marketHistoryJobResult.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, marketHistoryJobResult.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!marketHistoryJobResultRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        MarketHistoryJobResult result = marketHistoryJobResultRepository.save(marketHistoryJobResult);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, marketHistoryJobResult.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /market-history-job-results/:id} : Partial updates given fields of an existing marketHistoryJobResult, field will ignore if it is null
     *
     * @param id the id of the marketHistoryJobResult to save.
     * @param marketHistoryJobResult the marketHistoryJobResult to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated marketHistoryJobResult,
     * or with status {@code 400 (Bad Request)} if the marketHistoryJobResult is not valid,
     * or with status {@code 404 (Not Found)} if the marketHistoryJobResult is not found,
     * or with status {@code 500 (Internal Server Error)} if the marketHistoryJobResult couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/market-history-job-results/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<MarketHistoryJobResult> partialUpdateMarketHistoryJobResult(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MarketHistoryJobResult marketHistoryJobResult
    ) throws URISyntaxException {
        log.debug("REST request to partial update MarketHistoryJobResult partially : {}, {}", id, marketHistoryJobResult);
        if (marketHistoryJobResult.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, marketHistoryJobResult.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!marketHistoryJobResultRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MarketHistoryJobResult> result = marketHistoryJobResultRepository
            .findById(marketHistoryJobResult.getId())
            .map(existingMarketHistoryJobResult -> {
                if (marketHistoryJobResult.getIsSuccess() != null) {
                    existingMarketHistoryJobResult.setIsSuccess(marketHistoryJobResult.getIsSuccess());
                }
                if (marketHistoryJobResult.getTimeStart() != null) {
                    existingMarketHistoryJobResult.setTimeStart(marketHistoryJobResult.getTimeStart());
                }
                if (marketHistoryJobResult.getTimeEnd() != null) {
                    existingMarketHistoryJobResult.setTimeEnd(marketHistoryJobResult.getTimeEnd());
                }
                if (marketHistoryJobResult.getError() != null) {
                    existingMarketHistoryJobResult.setError(marketHistoryJobResult.getError());
                }
                if (marketHistoryJobResult.getEventId() != null) {
                    existingMarketHistoryJobResult.setEventId(marketHistoryJobResult.getEventId());
                }
                if (marketHistoryJobResult.getSymbols() != null) {
                    existingMarketHistoryJobResult.setSymbols(marketHistoryJobResult.getSymbols());
                }

                return existingMarketHistoryJobResult;
            })
            .map(marketHistoryJobResultRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, marketHistoryJobResult.getId().toString())
        );
    }

    /**
     * {@code GET  /market-history-job-results} : get all the marketHistoryJobResults.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of marketHistoryJobResults in body.
     */
    @GetMapping("/market-history-job-results")
    public List<MarketHistoryJobResult> getAllMarketHistoryJobResults(
        @RequestParam(required = false, defaultValue = "false") boolean eagerload
    ) {
        log.debug("REST request to get all MarketHistoryJobResults");
        if (eagerload) {
            return marketHistoryJobResultRepository.findAllWithEagerRelationships();
        } else {
            return marketHistoryJobResultRepository.findAll();
        }
    }

    /**
     * {@code GET  /market-history-job-results/:id} : get the "id" marketHistoryJobResult.
     *
     * @param id the id of the marketHistoryJobResult to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the marketHistoryJobResult, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/market-history-job-results/{id}")
    public ResponseEntity<MarketHistoryJobResult> getMarketHistoryJobResult(@PathVariable Long id) {
        log.debug("REST request to get MarketHistoryJobResult : {}", id);
        Optional<MarketHistoryJobResult> marketHistoryJobResult = marketHistoryJobResultRepository.findOneWithEagerRelationships(id);
        return ResponseUtil.wrapOrNotFound(marketHistoryJobResult);
    }

    /**
     * {@code DELETE  /market-history-job-results/:id} : delete the "id" marketHistoryJobResult.
     *
     * @param id the id of the marketHistoryJobResult to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/market-history-job-results/{id}")
    public ResponseEntity<Void> deleteMarketHistoryJobResult(@PathVariable Long id) {
        log.debug("REST request to delete MarketHistoryJobResult : {}", id);
        marketHistoryJobResultRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
