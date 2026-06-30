package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.repository.CopyTradingRegisterRepository;
import com.difisoft.nhsv.admin.service.CopyTradingRegisterQueryService;
import com.difisoft.nhsv.admin.service.CopyTradingRegisterService;
import com.difisoft.nhsv.admin.service.criteria.CopyTradingRegisterCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyTradingRegisterDTO;
import com.difisoft.nhsv.admin.web.rest.errors.BadRequestAlertException;
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CopyTradingRegister}.
 */
@RestController
@RequestMapping("/api")
public class CopyTradingRegisterResource {
    private final Logger log = LoggerFactory.getLogger(CopyTradingRegisterResource.class);
    private static final String ENTITY_NAME = "copyTradingRegister";
    @Value("${jhipster.clientApp.name}")
    private String applicationName;
    private final CopyTradingRegisterService copyTradingRegisterService;
    private final CopyTradingRegisterRepository copyTradingRegisterRepository;
    private final CopyTradingRegisterQueryService copyTradingRegisterQueryService;

    public CopyTradingRegisterResource(
        CopyTradingRegisterService copyTradingRegisterService,
        CopyTradingRegisterRepository copyTradingRegisterRepository,
        CopyTradingRegisterQueryService copyTradingRegisterQueryService
    ) {
        this.copyTradingRegisterService = copyTradingRegisterService;
        this.copyTradingRegisterRepository = copyTradingRegisterRepository;
        this.copyTradingRegisterQueryService = copyTradingRegisterQueryService;
    }

    /**
     * {@code POST  /copy-trading-registers} : Create a new copyTradingRegister.
     *
     * @param copyTradingRegisterDTO the copyTradingRegisterDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new copyTradingRegisterDTO, or with status {@code 400 (Bad Request)} if the copyTradingRegister has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/copy-trading-registers")
    public ResponseEntity<CopyTradingRegisterDTO> createCopyTradingRegister(@RequestBody CopyTradingRegisterDTO copyTradingRegisterDTO)
        throws URISyntaxException {
        log.debug("REST request to save CopyTradingRegister : {}", copyTradingRegisterDTO);
        if (copyTradingRegisterDTO.getId() != null) {
            throw new BadRequestAlertException("A new copyTradingRegister cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CopyTradingRegisterDTO result = copyTradingRegisterService.save(copyTradingRegisterDTO);
        return ResponseEntity
            .created(new URI("/api/copy-trading-registers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /copy-trading-registers/:id} : Updates an existing copyTradingRegister.
     *
     * @param id                     the id of the copyTradingRegisterDTO to save.
     * @param copyTradingRegisterDTO the copyTradingRegisterDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyTradingRegisterDTO,
     * or with status {@code 400 (Bad Request)} if the copyTradingRegisterDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the copyTradingRegisterDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/copy-trading-registers/{id}")
    public ResponseEntity<CopyTradingRegisterDTO> updateCopyTradingRegister(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody CopyTradingRegisterDTO copyTradingRegisterDTO
    ) throws URISyntaxException {
        log.debug("REST request to update CopyTradingRegister : {}, {}", id, copyTradingRegisterDTO);
        if (copyTradingRegisterDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyTradingRegisterDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!copyTradingRegisterRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        CopyTradingRegisterDTO result = copyTradingRegisterService.update(copyTradingRegisterDTO);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyTradingRegisterDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /copy-trading-registers/:id} : Partial updates given fields of an existing copyTradingRegister, field will ignore if it is null
     *
     * @param id                     the id of the copyTradingRegisterDTO to save.
     * @param copyTradingRegisterDTO the copyTradingRegisterDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated copyTradingRegisterDTO,
     * or with status {@code 400 (Bad Request)} if the copyTradingRegisterDTO is not valid,
     * or with status {@code 404 (Not Found)} if the copyTradingRegisterDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the copyTradingRegisterDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/copy-trading-registers/{id}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<CopyTradingRegisterDTO> partialUpdateCopyTradingRegister(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody CopyTradingRegisterDTO copyTradingRegisterDTO
    ) throws URISyntaxException {
        log.debug("REST request to partial update CopyTradingRegister partially : {}, {}", id, copyTradingRegisterDTO);
        if (copyTradingRegisterDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, copyTradingRegisterDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!copyTradingRegisterRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        Optional<CopyTradingRegisterDTO> result = copyTradingRegisterService.partialUpdate(copyTradingRegisterDTO);
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, copyTradingRegisterDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /copy-trading-registers} : get all the copyTradingRegisters.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of copyTradingRegisters in body.
     */
    @GetMapping("/copy-trading-registers")
    public ResponseEntity<List<CopyTradingRegisterDTO>> getAllCopyTradingRegisters(
        CopyTradingRegisterCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CopyTradingRegisters by criteria: {}", criteria);
        Page<CopyTradingRegisterDTO> page = copyTradingRegisterQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /copy-trading-registers/count} : count all the copyTradingRegisters.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/copy-trading-registers/count")
    public ResponseEntity<Long> countCopyTradingRegisters(CopyTradingRegisterCriteria criteria) {
        log.debug("REST request to count CopyTradingRegisters by criteria: {}", criteria);
        return ResponseEntity.ok().body(copyTradingRegisterQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /copy-trading-registers/:id} : get the "id" copyTradingRegister.
     *
     * @param id the id of the copyTradingRegisterDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the copyTradingRegisterDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/copy-trading-registers/{id}")
    public ResponseEntity<CopyTradingRegisterDTO> getCopyTradingRegister(@PathVariable Long id) {
        log.debug("REST request to get CopyTradingRegister : {}", id);
        Optional<CopyTradingRegisterDTO> copyTradingRegisterDTO = copyTradingRegisterService.findOne(id);
        return ResponseUtil.wrapOrNotFound(copyTradingRegisterDTO);
    }

    /**
     * {@code DELETE  /copy-trading-registers/:id} : delete the "id" copyTradingRegister.
     *
     * @param id the id of the copyTradingRegisterDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/copy-trading-registers/{id}")
    public ResponseEntity<Void> deleteCopyTradingRegister(@PathVariable Long id) {
        log.debug("REST request to delete CopyTradingRegister : {}", id);
        copyTradingRegisterService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
