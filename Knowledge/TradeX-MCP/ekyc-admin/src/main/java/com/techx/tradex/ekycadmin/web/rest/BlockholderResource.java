package com.techx.tradex.ekycadmin.web.rest;

import com.techx.tradex.ekycadmin.domain.Blockholder;
import com.techx.tradex.ekycadmin.repository.BlockholderRepository;
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
 * REST controller for managing {@link com.techx.tradex.ekycadmin.domain.Blockholder}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class BlockholderResource {

    private final Logger log = LoggerFactory.getLogger(BlockholderResource.class);

    private static final String ENTITY_NAME = "blockholder";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final BlockholderRepository blockholderRepository;

    public BlockholderResource(BlockholderRepository blockholderRepository) {
        this.blockholderRepository = blockholderRepository;
    }

    /**
     * {@code POST  /blockholders} : Create a new blockholder.
     *
     * @param blockholder the blockholder to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new blockholder, or with status {@code 400 (Bad Request)} if the blockholder has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/blockholders")
    public ResponseEntity<Blockholder> createBlockholder(@RequestBody Blockholder blockholder) throws URISyntaxException {
        log.debug("REST request to save Blockholder : {}", blockholder);
        if (blockholder.getId() != null) {
            throw new BadRequestAlertException("A new blockholder cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Blockholder result = blockholderRepository.save(blockholder);
        return ResponseEntity
            .created(new URI("/api/blockholders/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /blockholders/:id} : Updates an existing blockholder.
     *
     * @param id the id of the blockholder to save.
     * @param blockholder the blockholder to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated blockholder,
     * or with status {@code 400 (Bad Request)} if the blockholder is not valid,
     * or with status {@code 500 (Internal Server Error)} if the blockholder couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/blockholders/{id}")
    public ResponseEntity<Blockholder> updateBlockholder(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Blockholder blockholder
    ) throws URISyntaxException {
        log.debug("REST request to update Blockholder : {}, {}", id, blockholder);
        if (blockholder.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, blockholder.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!blockholderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Blockholder result = blockholderRepository.save(blockholder);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, blockholder.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /blockholders/:id} : Partial updates given fields of an existing blockholder, field will ignore if it is null
     *
     * @param id the id of the blockholder to save.
     * @param blockholder the blockholder to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated blockholder,
     * or with status {@code 400 (Bad Request)} if the blockholder is not valid,
     * or with status {@code 404 (Not Found)} if the blockholder is not found,
     * or with status {@code 500 (Internal Server Error)} if the blockholder couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/blockholders/{id}", consumes = "application/merge-patch+json")
    public ResponseEntity<Blockholder> partialUpdateBlockholder(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody Blockholder blockholder
    ) throws URISyntaxException {
        log.debug("REST request to partial update Blockholder partially : {}, {}", id, blockholder);
        if (blockholder.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, blockholder.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!blockholderRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Blockholder> result = blockholderRepository
            .findById(blockholder.getId())
            .map(
                existingBlockholder -> {
                    if (blockholder.getCompanyName() != null) {
                        existingBlockholder.setCompanyName(blockholder.getCompanyName());
                    }
                    if (blockholder.getStock() != null) {
                        existingBlockholder.setStock(blockholder.getStock());
                    }
                    if (blockholder.getPosition() != null) {
                        existingBlockholder.setPosition(blockholder.getPosition());
                    }

                    return existingBlockholder;
                }
            )
            .map(blockholderRepository::save);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, blockholder.getId().toString())
        );
    }

    /**
     * {@code GET  /blockholders} : get all the blockholders.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of blockholders in body.
     */
    @GetMapping("/blockholders")
    public List<Blockholder> getAllBlockholders() {
        log.debug("REST request to get all Blockholders");
        return blockholderRepository.findAll();
    }

    /**
     * {@code GET  /blockholders/:id} : get the "id" blockholder.
     *
     * @param id the id of the blockholder to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the blockholder, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/blockholders/{id}")
    public ResponseEntity<Blockholder> getBlockholder(@PathVariable Long id) {
        log.debug("REST request to get Blockholder : {}", id);
        Optional<Blockholder> blockholder = blockholderRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(blockholder);
    }

    /**
     * {@code DELETE  /blockholders/:id} : delete the "id" blockholder.
     *
     * @param id the id of the blockholder to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/blockholders/{id}")
    public ResponseEntity<Void> deleteBlockholder(@PathVariable Long id) {
        log.debug("REST request to delete Blockholder : {}", id);
        blockholderRepository.deleteById(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
            .build();
    }
}
