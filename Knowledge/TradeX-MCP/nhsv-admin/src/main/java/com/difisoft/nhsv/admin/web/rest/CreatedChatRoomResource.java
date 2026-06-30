package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.domain.CreatedChatRoom;
import com.difisoft.nhsv.admin.repository.CreatedChatRoomRepository;
import com.difisoft.nhsv.admin.service.CreatedChatRoomQueryService;
import com.difisoft.nhsv.admin.service.CreatedChatRoomService;
import com.difisoft.nhsv.admin.service.criteria.CreatedChatRoomCriteria;
import com.difisoft.nhsv.admin.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.CreatedChatRoom}.
 */
@RestController
@RequestMapping("/api")
public class CreatedChatRoomResource {

    private final Logger log = LoggerFactory.getLogger(CreatedChatRoomResource.class);

    private static final String ENTITY_NAME = "createdChatRoom";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CreatedChatRoomService createdChatRoomService;

    private final CreatedChatRoomRepository createdChatRoomRepository;

    private final CreatedChatRoomQueryService createdChatRoomQueryService;

    public CreatedChatRoomResource(
        CreatedChatRoomService createdChatRoomService,
        CreatedChatRoomRepository createdChatRoomRepository,
        CreatedChatRoomQueryService createdChatRoomQueryService
    ) {
        this.createdChatRoomService = createdChatRoomService;
        this.createdChatRoomRepository = createdChatRoomRepository;
        this.createdChatRoomQueryService = createdChatRoomQueryService;
    }

    /**
     * {@code POST  /created-chat-rooms} : Create a new createdChatRoom.
     *
     * @param createdChatRoom the createdChatRoom to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new createdChatRoom, or with status {@code 400 (Bad Request)} if the createdChatRoom has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/created-chat-rooms")
    public ResponseEntity<CreatedChatRoom> createCreatedChatRoom(@RequestBody CreatedChatRoom createdChatRoom) throws URISyntaxException {
        log.debug("REST request to save CreatedChatRoom : {}", createdChatRoom);
        if (createdChatRoom.getId() != null) {
            throw new BadRequestAlertException("A new createdChatRoom cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CreatedChatRoom result = createdChatRoomService.save(createdChatRoom);
        return ResponseEntity
            .created(new URI("/api/created-chat-rooms/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /created-chat-rooms/:id} : Updates an existing createdChatRoom.
     *
     * @param id the id of the createdChatRoom to save.
     * @param createdChatRoom the createdChatRoom to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated createdChatRoom,
     * or with status {@code 400 (Bad Request)} if the createdChatRoom is not valid,
     * or with status {@code 500 (Internal Server Error)} if the createdChatRoom couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/created-chat-rooms/{id}")
    public ResponseEntity<CreatedChatRoom> updateCreatedChatRoom(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody CreatedChatRoom createdChatRoom
    ) throws URISyntaxException {
        log.debug("REST request to update CreatedChatRoom : {}, {}", id, createdChatRoom);
        if (createdChatRoom.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, createdChatRoom.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!createdChatRoomRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        CreatedChatRoom result = createdChatRoomService.update(createdChatRoom);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, createdChatRoom.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /created-chat-rooms/:id} : Partial updates given fields of an existing createdChatRoom, field will ignore if it is null
     *
     * @param id the id of the createdChatRoom to save.
     * @param createdChatRoom the createdChatRoom to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated createdChatRoom,
     * or with status {@code 400 (Bad Request)} if the createdChatRoom is not valid,
     * or with status {@code 404 (Not Found)} if the createdChatRoom is not found,
     * or with status {@code 500 (Internal Server Error)} if the createdChatRoom couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/created-chat-rooms/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CreatedChatRoom> partialUpdateCreatedChatRoom(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody CreatedChatRoom createdChatRoom
    ) throws URISyntaxException {
        log.debug("REST request to partial update CreatedChatRoom partially : {}, {}", id, createdChatRoom);
        if (createdChatRoom.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, createdChatRoom.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!createdChatRoomRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CreatedChatRoom> result = createdChatRoomService.partialUpdate(createdChatRoom);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, createdChatRoom.getId().toString())
        );
    }

    /**
     * {@code GET  /created-chat-rooms} : get all the createdChatRooms.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of createdChatRooms in body.
     */
    @GetMapping("/created-chat-rooms")
    public ResponseEntity<List<CreatedChatRoom>> getAllCreatedChatRooms(
        CreatedChatRoomCriteria criteria,
        @org.springdoc.api.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get CreatedChatRooms by criteria: {}", criteria);
        Page<CreatedChatRoom> page = createdChatRoomQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /created-chat-rooms/count} : count all the createdChatRooms.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/created-chat-rooms/count")
    public ResponseEntity<Long> countCreatedChatRooms(CreatedChatRoomCriteria criteria) {
        log.debug("REST request to count CreatedChatRooms by criteria: {}", criteria);
        return ResponseEntity.ok().body(createdChatRoomQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /created-chat-rooms/:id} : get the "id" createdChatRoom.
     *
     * @param id the id of the createdChatRoom to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the createdChatRoom, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/created-chat-rooms/{id}")
    public ResponseEntity<CreatedChatRoom> getCreatedChatRoom(@PathVariable Long id) {
        log.debug("REST request to get CreatedChatRoom : {}", id);
        Optional<CreatedChatRoom> createdChatRoom = createdChatRoomService.findOne(id);
        return ResponseUtil.wrapOrNotFound(createdChatRoom);
    }

    /**
     * {@code DELETE  /created-chat-rooms/:id} : delete the "id" createdChatRoom.
     *
     * @param id the id of the createdChatRoom to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/created-chat-rooms/{id}")
    public ResponseEntity<Void> deleteCreatedChatRoom(@PathVariable Long id) {
        log.debug("REST request to delete CreatedChatRoom : {}", id);
        createdChatRoomService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
