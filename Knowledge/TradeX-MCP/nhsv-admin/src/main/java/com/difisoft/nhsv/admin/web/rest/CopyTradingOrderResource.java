package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.repository.CopyTradingOrderRepository;
import com.difisoft.nhsv.admin.service.CopyTradingOrderQueryService;
import com.difisoft.nhsv.admin.service.CopyTradingOrderService;
import com.difisoft.nhsv.admin.service.criteria.CopyTradingOrderCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyTradingOrderDTO;
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
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CopyTradingOrder}.
 */
@RestController
@RequestMapping("/api")
public class CopyTradingOrderResource {

    private final Logger log = LoggerFactory.getLogger(CopyTradingOrderResource.class);

    private static final String ENTITY_NAME = "copyTradingOrder";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CopyTradingOrderService copyTradingOrderService;

    private final CopyTradingOrderRepository copyTradingOrderRepository;

    private final CopyTradingOrderQueryService copyTradingOrderQueryService;

    public CopyTradingOrderResource(
        CopyTradingOrderService copyTradingOrderService,
        CopyTradingOrderRepository copyTradingOrderRepository,
        CopyTradingOrderQueryService copyTradingOrderQueryService
    ) {
        this.copyTradingOrderService = copyTradingOrderService;
        this.copyTradingOrderRepository = copyTradingOrderRepository;
        this.copyTradingOrderQueryService = copyTradingOrderQueryService;
    }

    /**
     * {@code POST  /copy-trading-orders} : Create a new copyTradingOrder.
     *
     * @param copyTradingOrderDTO the copyTradingOrderDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new copyTradingOrderDTO, or with status {@code 400 (Bad Request)} if the copyTradingOrder has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/copy-trading-orders")
    public ResponseEntity<CopyTradingOrderDTO> createCopyTradingOrder(@Valid @RequestBody CopyTradingOrderDTO copyTradingOrderDTO)
        throws URISyntaxException {
        log.debug("REST request to save CopyTradingOrder : {}", copyTradingOrderDTO);
        if (copyTradingOrderDTO.getId() != null) {
            throw new BadRequestAlertException("A new copyTradingOrder cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CopyTradingOrderDTO result = copyTradingOrderService.save(copyTradingOrderDTO);
        return ResponseEntity
            .created(new URI("/api/copy-trading-orders/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /copy-trading-orders/:id} : Updates an existing copyTradingOrder.
     *
     * @param id the id of the copyTradingOrderDTO to save.
     * @param copyTradingOrderDTO the copyTradingOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyTradingOrderDTO,
     * or with status {@code 400 (Bad Request)} if the copyTradingOrderDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the copyTradingOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/copy-trading-orders/{id}")
    public ResponseEntity<CopyTradingOrderDTO> updateCopyTradingOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CopyTradingOrderDTO copyTradingOrderDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CopyTradingOrder : {}, {}", id, copyTradingOrderDTO);
        if (copyTradingOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyTradingOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyTradingOrderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CopyTradingOrderDTO result = copyTradingOrderService.update(copyTradingOrderDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyTradingOrderDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /copy-trading-orders/:id} : Partial updates given fields of an existing copyTradingOrder, field will ignore if it is null
     *
     * @param id the id of the copyTradingOrderDTO to save.
     * @param copyTradingOrderDTO the copyTradingOrderDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyTradingOrderDTO,
     * or with status {@code 400 (Bad Request)} if the copyTradingOrderDTO is not valid,
     * or with status {@code 404 (Not Found)} if the copyTradingOrderDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the copyTradingOrderDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/copy-trading-orders/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CopyTradingOrderDTO> partialUpdateCopyTradingOrder(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CopyTradingOrderDTO copyTradingOrderDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CopyTradingOrder partially : {}, {}", id, copyTradingOrderDTO);
        if (copyTradingOrderDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyTradingOrderDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyTradingOrderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CopyTradingOrderDTO> result = copyTradingOrderService.partialUpdate(copyTradingOrderDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyTradingOrderDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /copy-trading-orders} : get all the copyTradingOrders.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of copyTradingOrders in body.
     */
    @GetMapping("/copy-trading-orders")
    public ResponseEntity<List<CopyTradingOrderDTO>> getAllCopyTradingOrders(
        CopyTradingOrderCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CopyTradingOrders by criteria: {}", criteria);
        Page<CopyTradingOrderDTO> page = copyTradingOrderQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /copy-trading-orders/count} : count all the copyTradingOrders.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/copy-trading-orders/count")
    public ResponseEntity<Long> countCopyTradingOrders(CopyTradingOrderCriteria criteria) {
        log.debug("REST request to count CopyTradingOrders by criteria: {}", criteria);
        return ResponseEntity.ok().body(copyTradingOrderQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /copy-trading-orders/:id} : get the "id" copyTradingOrder.
     *
     * @param id the id of the copyTradingOrderDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the copyTradingOrderDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/copy-trading-orders/{id}")
    public ResponseEntity<CopyTradingOrderDTO> getCopyTradingOrder(@PathVariable Long id) {
        log.debug("REST request to get CopyTradingOrder : {}", id);
        Optional<CopyTradingOrderDTO> copyTradingOrderDTO = copyTradingOrderService.findOne(id);
        return ResponseUtil.wrapOrNotFound(copyTradingOrderDTO);
    }

    /**
     * {@code DELETE  /copy-trading-orders/:id} : delete the "id" copyTradingOrder.
     *
     * @param id the id of the copyTradingOrderDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/copy-trading-orders/{id}")
    public ResponseEntity<Void> deleteCopyTradingOrder(@PathVariable Long id) {
        log.debug("REST request to delete CopyTradingOrder : {}", id);
        copyTradingOrderService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
