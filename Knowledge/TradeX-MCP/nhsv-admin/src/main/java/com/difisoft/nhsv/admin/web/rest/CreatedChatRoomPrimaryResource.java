package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.domain.ChatRoom;
import com.difisoft.nhsv.admin.domain.CreatedChatRoom;
import com.difisoft.nhsv.admin.repository.CreatedChatRoomRepository;
import com.difisoft.nhsv.admin.repository.primary.ChatRoomPrimaryRepository;
import com.difisoft.nhsv.admin.security.AuthoritiesConstants;
import com.difisoft.nhsv.admin.security.SecurityUtils;
import com.difisoft.nhsv.admin.service.ChatRoomServiceImp;
import com.difisoft.nhsv.admin.service.CreatedChatRoomQueryService;
import com.difisoft.nhsv.admin.service.CreatedChatRoomService;
import com.difisoft.nhsv.admin.service.criteria.CreatedChatRoomCriteria;
import com.difisoft.nhsv.admin.service.dto.ChatRoomImageDTO;
import com.difisoft.nhsv.admin.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import tech.jhipster.service.filter.StringFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing
 * {@link com.difisoft.nhsv.admin.domain.CreatedChatRoom}.
 */
@RestController
@RequestMapping("/api/v2")
@Primary
public class CreatedChatRoomPrimaryResource {

    private final Logger log = LoggerFactory.getLogger(CreatedChatRoomResource.class);

    private static final String ENTITY_NAME = "createdChatRoom";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CreatedChatRoomService createdChatRoomService;

    private final CreatedChatRoomQueryService createdChatRoomQueryService;

    private final ChatRoomPrimaryRepository chatRoomRepository;

    private final ChatRoomServiceImp chatRoomService;

    public CreatedChatRoomPrimaryResource(
            CreatedChatRoomService createdChatRoomService,
            CreatedChatRoomRepository createdChatRoomRepository,
            CreatedChatRoomQueryService createdChatRoomQueryService,
            ChatRoomPrimaryRepository chatRoomRepository,
            ChatRoomServiceImp chatRoomService) {
        this.createdChatRoomService = createdChatRoomService;
        this.createdChatRoomQueryService = createdChatRoomQueryService;
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomService = chatRoomService;
    }

    @PostMapping("/created-chat-rooms")
    public ResponseEntity<CreatedChatRoom> createCreatedChatRoom(@RequestBody CreatedChatRoom createdChatRoom)
            throws URISyntaxException {
        log.debug("REST request to save CreatedChatRoom : {}", createdChatRoom);
        if (createdChatRoom.getId() != null) {
            throw new BadRequestAlertException("A new createdChatRoom cannot already have an ID", ENTITY_NAME,
                    "idexists");
        }
        CreatedChatRoom result = createdChatRoomService.save(createdChatRoom);
        return ResponseEntity
                .created(new URI("/api/created-chat-rooms/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME,
                        result.getId().toString()))
                .body(result);
    }

    @PostMapping("/created-chat-rooms/upload")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\" , \"" + AuthoritiesConstants.SUPER_ADMIN
            + "\" , \"" + AuthoritiesConstants.BROKER + "\")")
    public void uploadImage(@ModelAttribute ChatRoomImageDTO chatRoomImageDTO) {
        log.info("REST request to upload User image : {}", chatRoomImageDTO.getFile().getOriginalFilename());
        chatRoomService.uploadChatRoomImage(chatRoomImageDTO,true);
    }

    @PutMapping("/created-chat-rooms/{id}")
    public ResponseEntity<ChatRoom> updateCreatedChatRoom(
            @PathVariable(value = "id", required = false) final Long id,
            @RequestBody CreatedChatRoom createdChatRoom) throws URISyntaxException {
        log.debug("REST request to update CreatedChatRoom : {}, {}", id, createdChatRoom);
        if (createdChatRoom.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, createdChatRoom.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!chatRoomRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        ChatRoom result = chatRoomService.updateChatRoom(createdChatRoom);
        String message = "A chatRoom is updated with identifier " + createdChatRoom.getId()
                + ". New information will be updated after administrator approval";
        return ResponseEntity
                .ok()
                .headers(HeaderUtil.createAlert(applicationName, message, ENTITY_NAME))
                .body(result);
    }

    @GetMapping("/created-chat-rooms")
    public ResponseEntity<List<CreatedChatRoom>> getAllCreatedChatRooms(
            CreatedChatRoomCriteria criteria,
            @org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.debug("REST request to get CreatedChatRooms by criteria: {}", criteria);
        String name = SecurityUtils.getCurrentUserLogin().get();
        StringFilter filter = new StringFilter();
        filter.setEquals(name);
        criteria.setBrokerName(filter);
        Page<CreatedChatRoom> page = createdChatRoomQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/created-chat-rooms/count")
    public ResponseEntity<Long> countCreatedChatRooms(CreatedChatRoomCriteria criteria) {
        log.debug("REST request to count CreatedChatRooms by criteria: {}", criteria);
        return ResponseEntity.ok().body(createdChatRoomQueryService.countByCriteria(criteria));
    }

    @GetMapping("/created-chat-rooms/{id}")
    public ResponseEntity<CreatedChatRoom> getCreatedChatRoom(@PathVariable Long id) {
        log.debug("REST request to get CreatedChatRoom : {}", id);
        Optional<CreatedChatRoom> createdChatRoom = createdChatRoomService.findOne(id);
        return ResponseUtil.wrapOrNotFound(createdChatRoom);
    }

    @DeleteMapping("/created-chat-rooms/{id}")
    public ResponseEntity<Void> deleteCreatedChatRoom(@PathVariable Long id) {
        log.debug("REST request to delete CreatedChatRoom : {}", id);
        createdChatRoomService.delete(id);
        String message = "A chatRoom is deleted with identifier " + id
                + ". Chat room will be deleted after administrator approval";
        return ResponseEntity
                .noContent()
                .headers(HeaderUtil.createAlert(applicationName, message, ENTITY_NAME))
                .build();
    }
}
