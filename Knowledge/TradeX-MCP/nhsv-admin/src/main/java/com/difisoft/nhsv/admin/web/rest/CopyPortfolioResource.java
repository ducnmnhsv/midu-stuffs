package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.repository.CopyPortfolioRepository;
import com.difisoft.nhsv.admin.service.CopyPortfolioQueryService;
import com.difisoft.nhsv.admin.service.CopyPortfolioService;
import com.difisoft.nhsv.admin.service.criteria.CopyPortfolioCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDTO;
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
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CopyPortfolio}.
 */
@RestController
@RequestMapping("/api")
public class CopyPortfolioResource {

    private final Logger log = LoggerFactory.getLogger(CopyPortfolioResource.class);

    private static final String ENTITY_NAME = "copyPortfolio";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CopyPortfolioService copyPortfolioService;

    private final CopyPortfolioRepository copyPortfolioRepository;

    private final CopyPortfolioQueryService copyPortfolioQueryService;

    public CopyPortfolioResource(
        CopyPortfolioService copyPortfolioService,
        CopyPortfolioRepository copyPortfolioRepository,
        CopyPortfolioQueryService copyPortfolioQueryService
    ) {
        this.copyPortfolioService = copyPortfolioService;
        this.copyPortfolioRepository = copyPortfolioRepository;
        this.copyPortfolioQueryService = copyPortfolioQueryService;
    }

    /**
     * {@code POST  /copy-portfolios} : Create a new copyPortfolio.
     *
     * @param copyPortfolioDTO the copyPortfolioDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new copyPortfolioDTO, or with status {@code 400 (Bad Request)} if the copyPortfolio has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/copy-portfolios")
    public ResponseEntity<CopyPortfolioDTO> createCopyPortfolio(@Valid @RequestBody CopyPortfolioDTO copyPortfolioDTO)
        throws URISyntaxException {
        log.debug("REST request to save CopyPortfolio : {}", copyPortfolioDTO);
        if (copyPortfolioDTO.getId() != null) {
            throw new BadRequestAlertException("A new copyPortfolio cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CopyPortfolioDTO result = copyPortfolioService.save(copyPortfolioDTO);
        return ResponseEntity
            .created(new URI("/api/copy-portfolios/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /copy-portfolios/:id} : Updates an existing copyPortfolio.
     *
     * @param id the id of the copyPortfolioDTO to save.
     * @param copyPortfolioDTO the copyPortfolioDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyPortfolioDTO,
     * or with status {@code 400 (Bad Request)} if the copyPortfolioDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the copyPortfolioDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/copy-portfolios/{id}")
    public ResponseEntity<CopyPortfolioDTO> updateCopyPortfolio(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CopyPortfolioDTO copyPortfolioDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CopyPortfolio : {}, {}", id, copyPortfolioDTO);
        if (copyPortfolioDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyPortfolioDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyPortfolioRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CopyPortfolioDTO result = copyPortfolioService.update(copyPortfolioDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyPortfolioDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /copy-portfolios/:id} : Partial updates given fields of an existing copyPortfolio, field will ignore if it is null
     *
     * @param id the id of the copyPortfolioDTO to save.
     * @param copyPortfolioDTO the copyPortfolioDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyPortfolioDTO,
     * or with status {@code 400 (Bad Request)} if the copyPortfolioDTO is not valid,
     * or with status {@code 404 (Not Found)} if the copyPortfolioDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the copyPortfolioDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/copy-portfolios/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CopyPortfolioDTO> partialUpdateCopyPortfolio(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CopyPortfolioDTO copyPortfolioDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CopyPortfolio partially : {}, {}", id, copyPortfolioDTO);
        if (copyPortfolioDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyPortfolioDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!copyPortfolioRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CopyPortfolioDTO> result = copyPortfolioService.partialUpdate(copyPortfolioDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyPortfolioDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /copy-portfolios} : get all the copyPortfolios.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of copyPortfolios in body.
     */
    @GetMapping("/copy-portfolios")
    public ResponseEntity<List<CopyPortfolioDTO>> getAllCopyPortfolios(
        CopyPortfolioCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CopyPortfolios by criteria: {}", criteria);
        Page<CopyPortfolioDTO> page = copyPortfolioQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /copy-portfolios/count} : count all the copyPortfolios.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/copy-portfolios/count")
    public ResponseEntity<Long> countCopyPortfolios(CopyPortfolioCriteria criteria) {
        log.debug("REST request to count CopyPortfolios by criteria: {}", criteria);
        return ResponseEntity.ok().body(copyPortfolioQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /copy-portfolios/:id} : get the "id" copyPortfolio.
     *
     * @param id the id of the copyPortfolioDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the copyPortfolioDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/copy-portfolios/{id}")
    public ResponseEntity<CopyPortfolioDTO> getCopyPortfolio(@PathVariable Long id) {
        log.debug("REST request to get CopyPortfolio : {}", id);
        Optional<CopyPortfolioDTO> copyPortfolioDTO = copyPortfolioService.findOne(id);
        return ResponseUtil.wrapOrNotFound(copyPortfolioDTO);
    }

    /**
     * {@code DELETE  /copy-portfolios/:id} : delete the "id" copyPortfolio.
     *
     * @param id the id of the copyPortfolioDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/copy-portfolios/{id}")
    public ResponseEntity<Void> deleteCopyPortfolio(@PathVariable Long id) {
        log.debug("REST request to delete CopyPortfolio : {}", id);
        copyPortfolioService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
