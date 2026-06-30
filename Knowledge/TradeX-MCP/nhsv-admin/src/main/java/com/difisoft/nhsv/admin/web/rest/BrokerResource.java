package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.domain.Broker;
import com.difisoft.nhsv.admin.repository.BrokerRepository;
import com.difisoft.nhsv.admin.service.BrokerQueryService;
import com.difisoft.nhsv.admin.service.BrokerService;
import com.difisoft.nhsv.admin.service.criteria.BrokerCriteria;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.Broker}.
 */
@RestController
@RequestMapping("/api")
public class BrokerResource {

    private final Logger log = LoggerFactory.getLogger(BrokerResource.class);

    private static final String ENTITY_NAME = "broker";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BrokerService brokerService;

    private final BrokerRepository brokerRepository;

    private final BrokerQueryService brokerQueryService;

    public BrokerResource(BrokerService brokerService, BrokerRepository brokerRepository, BrokerQueryService brokerQueryService) {
        this.brokerService = brokerService;
        this.brokerRepository = brokerRepository;
        this.brokerQueryService = brokerQueryService;
    }

    /**
     * {@code POST  /brokers} : Create a new broker.
     *
     * @param broker the broker to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new broker, or with status {@code 400 (Bad Request)} if the broker has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/brokers")
    public ResponseEntity<Broker> createBroker(@Valid @RequestBody Broker broker) throws URISyntaxException {
        log.debug("REST request to save Broker : {}", broker);
        if (broker.getId() != null) {
            throw new BadRequestAlertException("A new broker cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Broker result = brokerService.save(broker);
        return ResponseEntity
            .created(new URI("/api/brokers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /brokers/:id} : Updates an existing broker.
     *
     * @param id the id of the broker to save.
     * @param broker the broker to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated broker,
     * or with status {@code 400 (Bad Request)} if the broker is not valid,
     * or with status {@code 500 (Internal Server Error)} if the broker couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/brokers/{id}")
    public ResponseEntity<Broker> updateBroker(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Broker broker
    ) throws URISyntaxException {
        log.debug("REST request to update Broker : {}, {}", id, broker);
        if (broker.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, broker.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!brokerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Broker result = brokerService.update(broker);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, broker.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /brokers/:id} : Partial updates given fields of an existing broker, field will ignore if it is null
     *
     * @param id the id of the broker to save.
     * @param broker the broker to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated broker,
     * or with status {@code 400 (Bad Request)} if the broker is not valid,
     * or with status {@code 404 (Not Found)} if the broker is not found,
     * or with status {@code 500 (Internal Server Error)} if the broker couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/brokers/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<Broker> partialUpdateBroker(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Broker broker
    ) throws URISyntaxException {
        log.debug("REST request to partial update Broker partially : {}, {}", id, broker);
        if (broker.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, broker.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!brokerRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Broker> result = brokerService.partialUpdate(broker);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, broker.getId().toString())
        );
    }

    /**
     * {@code GET  /brokers} : get all the brokers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of brokers in body.
     */
    @GetMapping("/brokers")
    public ResponseEntity<List<Broker>> getAllBrokers(BrokerCriteria criteria) {
        log.debug("REST request to get Brokers by criteria: {}", criteria);
        List<Broker> entityList = brokerQueryService.findByCriteria(criteria);
        return ResponseEntity.ok().body(entityList);
    }

    /**
     * {@code GET  /brokers/count} : count all the brokers.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/brokers/count")
    public ResponseEntity<Long> countBrokers(BrokerCriteria criteria) {
        log.debug("REST request to count Brokers by criteria: {}", criteria);
        return ResponseEntity.ok().body(brokerQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /brokers/:id} : get the "id" broker.
     *
     * @param id the id of the broker to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the broker, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/brokers/{id}")
    public ResponseEntity<Broker> getBroker(@PathVariable Long id) {
        log.debug("REST request to get Broker : {}", id);
        Optional<Broker> broker = brokerService.findOne(id);
        return ResponseUtil.wrapOrNotFound(broker);
    }

    /**
     * {@code DELETE  /brokers/:id} : delete the "id" broker.
     *
     * @param id the id of the broker to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/brokers/{id}")
    public ResponseEntity<Void> deleteBroker(@PathVariable Long id) {
        log.debug("REST request to delete Broker : {}", id);
        brokerService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
