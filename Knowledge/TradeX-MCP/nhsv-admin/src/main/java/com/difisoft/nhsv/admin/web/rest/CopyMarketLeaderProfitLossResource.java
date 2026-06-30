package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.repository.CopyMarketLeaderProfitLossRepository;
import com.difisoft.nhsv.admin.service.CopyMarketLeaderProfitLossService;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderProfitLossDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLoss}.
 */
@RestController
@RequestMapping("/api")
public class CopyMarketLeaderProfitLossResource {

    private final Logger log = LoggerFactory.getLogger(CopyMarketLeaderProfitLossResource.class);

    private static final String ENTITY_NAME = "copyMarketLeaderProfitLoss";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CopyMarketLeaderProfitLossService copyMarketLeaderProfitLossService;

    private final CopyMarketLeaderProfitLossRepository copyMarketLeaderProfitLossRepository;

    public CopyMarketLeaderProfitLossResource(
        CopyMarketLeaderProfitLossService copyMarketLeaderProfitLossService,
        CopyMarketLeaderProfitLossRepository copyMarketLeaderProfitLossRepository
    ) {
        this.copyMarketLeaderProfitLossService = copyMarketLeaderProfitLossService;
        this.copyMarketLeaderProfitLossRepository = copyMarketLeaderProfitLossRepository;
    }

    /**
     * {@code POST  /copy-market-leader-profit-losses} : Create a new copyMarketLeaderProfitLoss.
     *
     * @param copyMarketLeaderProfitLossDTO the copyMarketLeaderProfitLossDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new copyMarketLeaderProfitLossDTO, or with status {@code 400 (Bad Request)} if the copyMarketLeaderProfitLoss has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/copy-market-leader-profit-losses")
    public ResponseEntity<CopyMarketLeaderProfitLossDTO> createCopyMarketLeaderProfitLoss(
        @Valid @RequestBody CopyMarketLeaderProfitLossDTO copyMarketLeaderProfitLossDTO
    ) throws URISyntaxException {
        log.debug("REST request to save CopyMarketLeaderProfitLoss : {}", copyMarketLeaderProfitLossDTO);
        if (copyMarketLeaderProfitLossDTO.getId() != null) {
            throw new BadRequestAlertException("A new copyMarketLeaderProfitLoss cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CopyMarketLeaderProfitLossDTO result = copyMarketLeaderProfitLossService.save(copyMarketLeaderProfitLossDTO);
        return ResponseEntity
            .created(new URI("/api/copy-market-leader-profit-losses/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /copy-market-leader-profit-losses/:id} : Updates an existing copyMarketLeaderProfitLoss.
     *
     * @param id the id of the copyMarketLeaderProfitLossDTO to save.
     * @param copyMarketLeaderProfitLossDTO the copyMarketLeaderProfitLossDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyMarketLeaderProfitLossDTO,
     * or with status {@code 400 (Bad Request)} if the copyMarketLeaderProfitLossDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the copyMarketLeaderProfitLossDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/copy-market-leader-profit-losses/{id}")
    public ResponseEntity<CopyMarketLeaderProfitLossDTO> updateCopyMarketLeaderProfitLoss(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CopyMarketLeaderProfitLossDTO copyMarketLeaderProfitLossDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CopyMarketLeaderProfitLoss : {}, {}", id, copyMarketLeaderProfitLossDTO);
        if (copyMarketLeaderProfitLossDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyMarketLeaderProfitLossDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyMarketLeaderProfitLossRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CopyMarketLeaderProfitLossDTO result = copyMarketLeaderProfitLossService.update(copyMarketLeaderProfitLossDTO);
        return ResponseEntity
            .ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyMarketLeaderProfitLossDTO.getId().toString())
            )
            .body(result);
    }

    /**
     * {@code PATCH  /copy-market-leader-profit-losses/:id} : Partial updates given fields of an existing copyMarketLeaderProfitLoss, field will ignore if it is null
     *
     * @param id the id of the copyMarketLeaderProfitLossDTO to save.
     * @param copyMarketLeaderProfitLossDTO the copyMarketLeaderProfitLossDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyMarketLeaderProfitLossDTO,
     * or with status {@code 400 (Bad Request)} if the copyMarketLeaderProfitLossDTO is not valid,
     * or with status {@code 404 (Not Found)} if the copyMarketLeaderProfitLossDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the copyMarketLeaderProfitLossDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/copy-market-leader-profit-losses/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CopyMarketLeaderProfitLossDTO> partialUpdateCopyMarketLeaderProfitLoss(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CopyMarketLeaderProfitLossDTO copyMarketLeaderProfitLossDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CopyMarketLeaderProfitLoss partially : {}, {}", id, copyMarketLeaderProfitLossDTO);
        if (copyMarketLeaderProfitLossDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyMarketLeaderProfitLossDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyMarketLeaderProfitLossRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CopyMarketLeaderProfitLossDTO> result = copyMarketLeaderProfitLossService.partialUpdate(copyMarketLeaderProfitLossDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyMarketLeaderProfitLossDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /copy-market-leader-profit-losses} : get all the copyMarketLeaderProfitLosses.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of copyMarketLeaderProfitLosses in body.
     */
    @GetMapping("/copy-market-leader-profit-losses")
    public ResponseEntity<List<CopyMarketLeaderProfitLossDTO>> getAllCopyMarketLeaderProfitLosses(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get a page of CopyMarketLeaderProfitLosses");
        Page<CopyMarketLeaderProfitLossDTO> page = copyMarketLeaderProfitLossService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /copy-market-leader-profit-losses/:id} : get the "id" copyMarketLeaderProfitLoss.
     *
     * @param id the id of the copyMarketLeaderProfitLossDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the copyMarketLeaderProfitLossDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/copy-market-leader-profit-losses/{id}")
    public ResponseEntity<CopyMarketLeaderProfitLossDTO> getCopyMarketLeaderProfitLoss(@PathVariable Long id) {
        log.debug("REST request to get CopyMarketLeaderProfitLoss : {}", id);
        Optional<CopyMarketLeaderProfitLossDTO> copyMarketLeaderProfitLossDTO = copyMarketLeaderProfitLossService.findOne(id);
        return ResponseUtil.wrapOrNotFound(copyMarketLeaderProfitLossDTO);
    }

    /**
     * {@code DELETE  /copy-market-leader-profit-losses/:id} : delete the "id" copyMarketLeaderProfitLoss.
     *
     * @param id the id of the copyMarketLeaderProfitLossDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/copy-market-leader-profit-losses/{id}")
    public ResponseEntity<Void> deleteCopyMarketLeaderProfitLoss(@PathVariable Long id) {
        log.debug("REST request to delete CopyMarketLeaderProfitLoss : {}", id);
        copyMarketLeaderProfitLossService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
