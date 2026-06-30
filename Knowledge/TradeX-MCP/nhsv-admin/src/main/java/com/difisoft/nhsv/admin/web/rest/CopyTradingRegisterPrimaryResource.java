package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.domain.CopyTradingRegister;
import com.difisoft.nhsv.admin.repository.CopyTradingRegisterRepository;
import com.difisoft.nhsv.admin.service.CopyTradingRegisterPrimaryQueryService;
import com.difisoft.nhsv.admin.service.CopyTradingRegisterQueryService;
import com.difisoft.nhsv.admin.service.CopyTradingRegisterService;
import com.difisoft.nhsv.admin.service.criteria.CopyTradingRegisterCriteria;
import com.difisoft.nhsv.admin.service.criteria.CopyTradingRegisterPrimaryCriteria;
import com.difisoft.nhsv.admin.service.dto.CopyTradingRegisterDTO;
import com.difisoft.nhsv.admin.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/v2")
public class CopyTradingRegisterPrimaryResource {
    private final Logger log = LoggerFactory.getLogger(CopyTradingRegisterPrimaryResource.class);
    private static final String ENTITY_NAME = "copyTradingRegister";
    @Value("${jhipster.clientApp.name}")
    private String applicationName;
    private final CopyTradingRegisterService copyTradingRegisterService;
    private final CopyTradingRegisterRepository copyTradingRegisterRepository;
    private final CopyTradingRegisterQueryService copyTradingRegisterQueryService;
    private final CopyTradingRegisterPrimaryQueryService copyTradingRegisterPrimaryQueryService;

    public CopyTradingRegisterPrimaryResource(
        CopyTradingRegisterService copyTradingRegisterService,
        CopyTradingRegisterRepository copyTradingRegisterRepository,
        CopyTradingRegisterQueryService copyTradingRegisterQueryService,
        CopyTradingRegisterPrimaryQueryService copyTradingRegisterPrimaryQueryService
    ) {
        this.copyTradingRegisterService = copyTradingRegisterService;
        this.copyTradingRegisterRepository = copyTradingRegisterRepository;
        this.copyTradingRegisterQueryService = copyTradingRegisterQueryService;
        this.copyTradingRegisterPrimaryQueryService = copyTradingRegisterPrimaryQueryService;
    }


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

    @GetMapping("/copy-trading-registers/count")
    public ResponseEntity<Long> countCopyTradingRegisters(CopyTradingRegisterCriteria criteria) {
        log.debug("REST request to count CopyTradingRegisters by criteria: {}", criteria);
        return ResponseEntity.ok().body(copyTradingRegisterQueryService.countByCriteria(criteria));
    }

    @GetMapping("/copy-trading-registers/{id}")
    public ResponseEntity<CopyTradingRegisterDTO> getCopyTradingRegister(@PathVariable Long id) {
        log.debug("REST request to get CopyTradingRegister : {}", id);
        Optional<CopyTradingRegisterDTO> copyTradingRegisterDTO = copyTradingRegisterService.findOne(id);
        return ResponseUtil.wrapOrNotFound(copyTradingRegisterDTO);
    }

    @DeleteMapping("/copy-trading-registers/{id}")
    public ResponseEntity<Void> deleteCopyTradingRegister(@PathVariable Long id) {
        log.debug("REST request to delete CopyTradingRegister : {}", id);
        copyTradingRegisterService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    @PostMapping("/download-copy-trading-registers")
    public ResponseEntity<InputStreamResource> downloadChatRoomsExcel(
        CopyTradingRegisterPrimaryCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.info("haha: {}", criteria);
        Page<CopyTradingRegister> chatRoomsPage = copyTradingRegisterPrimaryQueryService.findByCriteria(criteria, pageable);
        LocalDate startDate = criteria.getUpdatedAt().getGreaterThanOrEqual().toLocalDate();
        LocalDate endDate = criteria.getUpdatedAt().getLessThanOrEqual().toLocalDate();
        Boolean status = null;
        if (criteria.getStatus() != null) {
            status = criteria.getStatus().getEquals();
        }
        String accountNumber = criteria.getAccountNumber() != null ? criteria.getAccountNumber().getEquals() : null;
        String subAccount = criteria.getSubAccount() != null ? criteria.getSubAccount().getContains() : null;
        ByteArrayInputStream in = copyTradingRegisterPrimaryQueryService.exportChatRoomsToExcel(
            chatRoomsPage.getContent(), startDate, endDate, status, accountNumber, subAccount);

        String filename = "copy_trading_register.xlsx";
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(file);
    }
}
