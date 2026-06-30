package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.repository.CopyMarketLeaderProfitLossDetailsRepository;
import com.difisoft.nhsv.admin.service.CopyMarketLeaderProfitLossDetailsService;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderProfitLossDetailsDTO;
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
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLossDetails}.
 */
@RestController
@RequestMapping("/api")
public class CopyMarketLeaderProfitLossDetailsResource {

    private final Logger log = LoggerFactory.getLogger(CopyMarketLeaderProfitLossDetailsResource.class);

    private static final String ENTITY_NAME = "copyMarketLeaderProfitLossDetails";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CopyMarketLeaderProfitLossDetailsService copyMarketLeaderProfitLossDetailsService;

    private final CopyMarketLeaderProfitLossDetailsRepository copyMarketLeaderProfitLossDetailsRepository;

    public CopyMarketLeaderProfitLossDetailsResource(
        CopyMarketLeaderProfitLossDetailsService copyMarketLeaderProfitLossDetailsService,
        CopyMarketLeaderProfitLossDetailsRepository copyMarketLeaderProfitLossDetailsRepository
    ) {
        this.copyMarketLeaderProfitLossDetailsService = copyMarketLeaderProfitLossDetailsService;
        this.copyMarketLeaderProfitLossDetailsRepository = copyMarketLeaderProfitLossDetailsRepository;
    }

    /**
     * {@code POST  /copy-market-leader-profit-loss-details} : Create a new copyMarketLeaderProfitLossDetails.
     *
     * @param copyMarketLeaderProfitLossDetailsDTO the copyMarketLeaderProfitLossDetailsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new copyMarketLeaderProfitLossDetailsDTO, or with status {@code 400 (Bad Request)} if the copyMarketLeaderProfitLossDetails has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/copy-market-leader-profit-loss-details")
    public ResponseEntity<CopyMarketLeaderProfitLossDetailsDTO> createCopyMarketLeaderProfitLossDetails(
        @Valid @RequestBody CopyMarketLeaderProfitLossDetailsDTO copyMarketLeaderProfitLossDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to save CopyMarketLeaderProfitLossDetails : {}", copyMarketLeaderProfitLossDetailsDTO);
        if (copyMarketLeaderProfitLossDetailsDTO.getId() != null) {
            throw new BadRequestAlertException(
                "A new copyMarketLeaderProfitLossDetails cannot already have an ID",
                ENTITY_NAME,
                "idexists"
            );
        }
        CopyMarketLeaderProfitLossDetailsDTO result = copyMarketLeaderProfitLossDetailsService.save(copyMarketLeaderProfitLossDetailsDTO);
        return ResponseEntity
            .created(new URI("/api/copy-market-leader-profit-loss-details/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /copy-market-leader-profit-loss-details/:id} : Updates an existing copyMarketLeaderProfitLossDetails.
     *
     * @param id the id of the copyMarketLeaderProfitLossDetailsDTO to save.
     * @param copyMarketLeaderProfitLossDetailsDTO the copyMarketLeaderProfitLossDetailsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyMarketLeaderProfitLossDetailsDTO,
     * or with status {@code 400 (Bad Request)} if the copyMarketLeaderProfitLossDetailsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the copyMarketLeaderProfitLossDetailsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/copy-market-leader-profit-loss-details/{id}")
    public ResponseEntity<CopyMarketLeaderProfitLossDetailsDTO> updateCopyMarketLeaderProfitLossDetails(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CopyMarketLeaderProfitLossDetailsDTO copyMarketLeaderProfitLossDetailsDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CopyMarketLeaderProfitLossDetails : {}, {}", id, copyMarketLeaderProfitLossDetailsDTO);
        if (copyMarketLeaderProfitLossDetailsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyMarketLeaderProfitLossDetailsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyMarketLeaderProfitLossDetailsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CopyMarketLeaderProfitLossDetailsDTO result = copyMarketLeaderProfitLossDetailsService.update(copyMarketLeaderProfitLossDetailsDTO);
        return ResponseEntity
            .ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName,
                    false,
                    ENTITY_NAME,
                    copyMarketLeaderProfitLossDetailsDTO.getId().toString()
                )
            )
            .body(result);
    }

    /**
     * {@code PATCH  /copy-market-leader-profit-loss-details/:id} : Partial updates given fields of an existing copyMarketLeaderProfitLossDetails, field will ignore if it is null
     *
     * @param id the id of the copyMarketLeaderProfitLossDetailsDTO to save.
     * @param copyMarketLeaderProfitLossDetailsDTO the copyMarketLeaderProfitLossDetailsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyMarketLeaderProfitLossDetailsDTO,
     * or with status {@code 400 (Bad Request)} if the copyMarketLeaderProfitLossDetailsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the copyMarketLeaderProfitLossDetailsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the copyMarketLeaderProfitLossDetailsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/copy-market-leader-profit-loss-details/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CopyMarketLeaderProfitLossDetailsDTO> partialUpdateCopyMarketLeaderProfitLossDetails(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CopyMarketLeaderProfitLossDetailsDTO copyMarketLeaderProfitLossDetailsDTO
    ) throws URISyntaxException {
        log.debug(
            "REST request to partial update CopyMarketLeaderProfitLossDetails partially : {}, {}",
            id,
            copyMarketLeaderProfitLossDetailsDTO
        );
        if (copyMarketLeaderProfitLossDetailsDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyMarketLeaderProfitLossDetailsDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyMarketLeaderProfitLossDetailsRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CopyMarketLeaderProfitLossDetailsDTO> result = copyMarketLeaderProfitLossDetailsService.partialUpdate(
            copyMarketLeaderProfitLossDetailsDTO
        );

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyMarketLeaderProfitLossDetailsDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /copy-market-leader-profit-loss-details} : get all the copyMarketLeaderProfitLossDetails.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of copyMarketLeaderProfitLossDetails in body.
     */
    @GetMapping("/copy-market-leader-profit-loss-details")
    public ResponseEntity<List<CopyMarketLeaderProfitLossDetailsDTO>> getAllCopyMarketLeaderProfitLossDetails(
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get a page of CopyMarketLeaderProfitLossDetails");
        Page<CopyMarketLeaderProfitLossDetailsDTO> page = copyMarketLeaderProfitLossDetailsService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /copy-market-leader-profit-loss-details/:id} : get the "id" copyMarketLeaderProfitLossDetails.
     *
     * @param id the id of the copyMarketLeaderProfitLossDetailsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the copyMarketLeaderProfitLossDetailsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/copy-market-leader-profit-loss-details/{id}")
    public ResponseEntity<CopyMarketLeaderProfitLossDetailsDTO> getCopyMarketLeaderProfitLossDetails(@PathVariable Long id) {
        log.debug("REST request to get CopyMarketLeaderProfitLossDetails : {}", id);
        Optional<CopyMarketLeaderProfitLossDetailsDTO> copyMarketLeaderProfitLossDetailsDTO = copyMarketLeaderProfitLossDetailsService.findOne(
            id
        );
        return ResponseUtil.wrapOrNotFound(copyMarketLeaderProfitLossDetailsDTO);
    }

    /**
     * {@code DELETE  /copy-market-leader-profit-loss-details/:id} : delete the "id" copyMarketLeaderProfitLossDetails.
     *
     * @param id the id of the copyMarketLeaderProfitLossDetailsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/copy-market-leader-profit-loss-details/{id}")
    public ResponseEntity<Void> deleteCopyMarketLeaderProfitLossDetails(@PathVariable Long id) {
        log.debug("REST request to delete CopyMarketLeaderProfitLossDetails : {}", id);
        copyMarketLeaderProfitLossDetailsService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
