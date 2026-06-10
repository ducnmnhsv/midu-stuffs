package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.domain.MatchingRate;
import com.techx.tradex.ekycadmin.repository.MatchingRateRepository;
import com.techx.tradex.ekycadmin.web.rest.errors.BadRequestAlertException;
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
 * REST controller for managing {@link com.techx.tradex.ekycadmin.domain.MatchingRate}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class MatchingRateResource {

    private final Logger log = LoggerFactory.getLogger(MatchingRateResource.class);

    private static final String ENTITY_NAME = "matchingRate";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MatchingRateRepository matchingRateRepository;

    public MatchingRateResource(MatchingRateRepository matchingRateRepository) {
        this.matchingRateRepository = matchingRateRepository;
    }

    /**
     * {@code POST  /matching-rates} : Create a new matchingRate.
     *
     * @param matchingRate the matchingRate to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new matchingRate, or with status {@code 400 (Bad Request)} if the matchingRate has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/matching-rates")
    public ResponseEntity<MatchingRate> createMatchingRate(@RequestBody MatchingRate matchingRate) throws URISyntaxException {
        log.debug("REST request to save MatchingRate : {}", matchingRate);
        if (matchingRate.getId() != null) {
            throw new BadRequestAlertException("A new matchingRate cannot already have an ID", ENTITY_NAME, "idexists");
        }
        MatchingRate result = matchingRateRepository.save(matchingRate);
        return ResponseEntity
            .created(new URI("/api/matching-rates/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /matching-rates/:id} : Updates an existing matchingRate.
     *
     * @param id the id of the matchingRate to save.
     * @param matchingRate the matchingRate to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated matchingRate,
     * or with status {@code 400 (Bad Request)} if the matchingRate is not valid,
     * or with status {@code 500 (Internal Server Error)} if the matchingRate couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/matching-rates/{id}")
    public ResponseEntity<MatchingRate> updateMatchingRate(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MatchingRate matchingRate
    ) throws URISyntaxException {
        log.debug("REST request to update MatchingRate : {}, {}", id, matchingRate);
        if (matchingRate.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, matchingRate.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!matchingRateRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        MatchingRate result = matchingRateRepository.save(matchingRate);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, matchingRate.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /matching-rates/:id} : Partial updates given fields of an existing matchingRate, field will ignore if it is null
     *
     * @param id the id of the matchingRate to save.
     * @param matchingRate the matchingRate to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated matchingRate,
     * or with status {@code 400 (Bad Request)} if the matchingRate is not valid,
     * or with status {@code 404 (Not Found)} if the matchingRate is not found,
     * or with status {@code 500 (Internal Server Error)} if the matchingRate couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/matching-rates/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<MatchingRate> partialUpdateMatchingRate(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody MatchingRate matchingRate
    ) throws URISyntaxException {
        log.debug("REST request to partial update MatchingRate partially : {}, {}", id, matchingRate);
        if (matchingRate.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, matchingRate.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!matchingRateRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<MatchingRate> result = matchingRateRepository
            .findById(matchingRate.getId())
            .map(
                existingMatchingRate -> {
                    if (matchingRate.getCore() != null) {
                        existingMatchingRate.setCore(matchingRate.getCore());
                    }
                    if (matchingRate.getMatchingRate() != null) {
                        existingMatchingRate.setMatchingRate(matchingRate.getMatchingRate());
                    }
                    if (matchingRate.getCreatedAt() != null) {
                        existingMatchingRate.setCreatedAt(matchingRate.getCreatedAt());
                    }
                    if (matchingRate.getUpdatedAt() != null) {
                        existingMatchingRate.setUpdatedAt(matchingRate.getUpdatedAt());
                    }

                    return existingMatchingRate;
                }
            )
            .map(matchingRateRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, matchingRate.getId().toString())
        );
    }

    /**
     * {@code GET  /matching-rates} : get all the matchingRates.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of matchingRates in body.
     */
    @GetMapping("/matching-rates")
    public List<MatchingRate> getAllMatchingRates() {
        log.debug("REST request to get all MatchingRates");
        return matchingRateRepository.findAll();
    }

    /**
     * {@code GET  /matching-rates/:id} : get the "id" matchingRate.
     *
     * @param id the id of the matchingRate to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the matchingRate, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/matching-rates/{id}")
    public ResponseEntity<MatchingRate> getMatchingRate(@PathVariable Long id) {
        log.debug("REST request to get MatchingRate : {}", id);
        Optional<MatchingRate> matchingRate = matchingRateRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(matchingRate);
    }

    /**
     * {@code DELETE  /matching-rates/:id} : delete the "id" matchingRate.
     *
     * @param id the id of the matchingRate to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/matching-rates/{id}")
    public ResponseEntity<Void> deleteMatchingRate(@PathVariable Long id) {
        log.debug("REST request to delete MatchingRate : {}", id);
        matchingRateRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
