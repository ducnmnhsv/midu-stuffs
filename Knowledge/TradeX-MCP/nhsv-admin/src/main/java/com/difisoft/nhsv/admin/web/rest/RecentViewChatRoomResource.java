package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.domain.RecentViewChatRoom;
import com.difisoft.nhsv.admin.repository.RecentViewChatRoomRepository;
import com.difisoft.nhsv.admin.service.RecentViewChatRoomService;
import com.difisoft.nhsv.admin.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.difisoft.nhsv.admin.domain.RecentViewChatRoom}.
 */
@RestController
@RequestMapping("/api")
public class RecentViewChatRoomResource {

    private final Logger log = LoggerFactory.getLogger(RecentViewChatRoomResource.class);

    private static final String ENTITY_NAME = "recentViewChatRoom";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final RecentViewChatRoomService recentViewChatRoomService;

    private final RecentViewChatRoomRepository recentViewChatRoomRepository;

    public RecentViewChatRoomResource(
        RecentViewChatRoomService recentViewChatRoomService,
        RecentViewChatRoomRepository recentViewChatRoomRepository
    ) {
        this.recentViewChatRoomService = recentViewChatRoomService;
        this.recentViewChatRoomRepository = recentViewChatRoomRepository;
    }

    /**
     * {@code POST  /recent-view-chat-rooms} : Create a new recentViewChatRoom.
     *
     * @param recentViewChatRoom the recentViewChatRoom to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new recentViewChatRoom, or with status {@code 400 (Bad Request)} if the recentViewChatRoom has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/recent-view-chat-rooms")
    public ResponseEntity<RecentViewChatRoom> createRecentViewChatRoom(@RequestBody RecentViewChatRoom recentViewChatRoom)
        throws URISyntaxException {
        log.debug("REST request to save RecentViewChatRoom : {}", recentViewChatRoom);
        if (recentViewChatRoom.getId() != null) {
            throw new BadRequestAlertException("A new recentViewChatRoom cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RecentViewChatRoom result = recentViewChatRoomService.save(recentViewChatRoom);
        return ResponseEntity
            .created(new URI("/api/recent-view-chat-rooms/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /recent-view-chat-rooms/:id} : Updates an existing recentViewChatRoom.
     *
     * @param id the id of the recentViewChatRoom to save.
     * @param recentViewChatRoom the recentViewChatRoom to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated recentViewChatRoom,
     * or with status {@code 400 (Bad Request)} if the recentViewChatRoom is not valid,
     * or with status {@code 500 (Internal Server Error)} if the recentViewChatRoom couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/recent-view-chat-rooms/{id}")
    public ResponseEntity<RecentViewChatRoom> updateRecentViewChatRoom(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody RecentViewChatRoom recentViewChatRoom
    ) throws URISyntaxException {
        log.debug("REST request to update RecentViewChatRoom : {}, {}", id, recentViewChatRoom);
        if (recentViewChatRoom.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, recentViewChatRoom.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!recentViewChatRoomRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        RecentViewChatRoom result = recentViewChatRoomService.update(recentViewChatRoom);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, recentViewChatRoom.getId().toString()))
            .body(result);
    }

    /**
     * {@code PATCH  /recent-view-chat-rooms/:id} : Partial updates given fields of an existing recentViewChatRoom, field will ignore if it is null
     *
     * @param id the id of the recentViewChatRoom to save.
     * @param recentViewChatRoom the recentViewChatRoom to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated recentViewChatRoom,
     * or with status {@code 400 (Bad Request)} if the recentViewChatRoom is not valid,
     * or with status {@code 404 (Not Found)} if the recentViewChatRoom is not found,
     * or with status {@code 500 (Internal Server Error)} if the recentViewChatRoom couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/recent-view-chat-rooms/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<RecentViewChatRoom> partialUpdateRecentViewChatRoom(
        @PathVariable(value = "id", required = false) final Long id,
        @RequestBody RecentViewChatRoom recentViewChatRoom
    ) throws URISyntaxException {
        log.debug("REST request to partial update RecentViewChatRoom partially : {}, {}", id, recentViewChatRoom);
        if (recentViewChatRoom.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, recentViewChatRoom.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!recentViewChatRoomRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<RecentViewChatRoom> result = recentViewChatRoomService.partialUpdate(recentViewChatRoom);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, recentViewChatRoom.getId().toString())
        );
    }

    /**
     * {@code GET  /recent-view-chat-rooms} : get all the recentViewChatRooms.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of recentViewChatRooms in body.
     */
    @GetMapping("/recent-view-chat-rooms")
    public List<RecentViewChatRoom> getAllRecentViewChatRooms() {
        log.debug("REST request to get all RecentViewChatRooms");
        return recentViewChatRoomService.findAll();
    }

    /**
     * {@code GET  /recent-view-chat-rooms/:id} : get the "id" recentViewChatRoom.
     *
     * @param id the id of the recentViewChatRoom to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the recentViewChatRoom, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/recent-view-chat-rooms/{id}")
    public ResponseEntity<RecentViewChatRoom> getRecentViewChatRoom(@PathVariable Long id) {
        log.debug("REST request to get RecentViewChatRoom : {}", id);
        Optional<RecentViewChatRoom> recentViewChatRoom = recentViewChatRoomService.findOne(id);
        return ResponseUtil.wrapOrNotFound(recentViewChatRoom);
    }

    /**
     * {@code DELETE  /recent-view-chat-rooms/:id} : delete the "id" recentViewChatRoom.
     *
     * @param id the id of the recentViewChatRoom to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/recent-view-chat-rooms/{id}")
    public ResponseEntity<Void> deleteRecentViewChatRoom(@PathVariable Long id) {
        log.debug("REST request to delete RecentViewChatRoom : {}", id);
        recentViewChatRoomService.delete(id);
        return ResponseEntity
            .noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
