package com.difisoft.nhsv.admin.web.rest;

import com.difisoft.nhsv.admin.domain.ChatRoom;
import com.difisoft.nhsv.admin.domain.enumeration.ActionEnum;
import com.difisoft.nhsv.admin.domain.enumeration.StatusEnum;
import com.difisoft.nhsv.admin.repository.primary.BrokerPrimaryRepository;
import com.difisoft.nhsv.admin.repository.primary.ChatRoomPrimaryRepository;
import com.difisoft.nhsv.admin.security.AuthoritiesConstants;
import com.difisoft.nhsv.admin.security.SecurityUtils;
import com.difisoft.nhsv.admin.service.ChatRoomPrimaryQueryService;
import com.difisoft.nhsv.admin.service.ChatRoomQueryService;
import com.difisoft.nhsv.admin.service.ChatRoomServiceImp;
import com.difisoft.nhsv.admin.service.criteria.ChatRoomCriteria;
import com.difisoft.nhsv.admin.service.criteria.ChatRoomPrimaryCriteria;
import com.difisoft.nhsv.admin.service.dto.ChatRoomImageDTO;
import com.difisoft.nhsv.admin.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

@RestController
@RequestMapping("/api/v2")
public class ChatRoomPrimaryResource {

    private final Logger log = LoggerFactory.getLogger(ChatRoomPrimaryResource.class);

    private static final String ENTITY_NAME = "chatRoom";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ChatRoomServiceImp chatRoomService;

    private final ChatRoomQueryService chatRoomQueryService;
    private final ChatRoomPrimaryQueryService chatRoomPrimaryQueryService;

    private final ChatRoomPrimaryRepository chatRoomRepository;
    private final BrokerPrimaryRepository brokerRepository;

    public ChatRoomPrimaryResource(
            ChatRoomServiceImp chatRoomService,
            ChatRoomQueryService chatRoomQueryService,
            ChatRoomPrimaryRepository chatRoomRepository,
            BrokerPrimaryRepository brokerRepository,
            ChatRoomPrimaryQueryService chatRoomPrimaryQueryService) {
        this.chatRoomService = chatRoomService;
        this.chatRoomQueryService = chatRoomQueryService;
        this.chatRoomRepository = chatRoomRepository;
        this.brokerRepository = brokerRepository;
        this.chatRoomPrimaryQueryService = chatRoomPrimaryQueryService;
    }

    @PostMapping("/chat-rooms")
    public ResponseEntity<ChatRoom> createChatRoom(@RequestBody ChatRoom chatRoom) throws URISyntaxException {
        log.info("REST request to save ChatRoom : {}", chatRoom);
        if (chatRoom.getId() != null) {
            throw new BadRequestAlertException("A new chatRoom cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ChatRoom result = chatRoomService.createChatRoom(chatRoom);
        HttpHeaders headers = new HttpHeaders();
        String encodedMessage = URLEncoder.encode(
                "A new chat room request has been sent with group name: " + result.getGroupName(),
                StandardCharsets.UTF_8);
        headers.add("X-" + applicationName + "-alert", encodedMessage);
        return ResponseEntity
                .created(new URI("/api/chat-rooms/" + result.getId()))
                .headers(headers)
                .body(result);
    }

    @PostMapping("/chat-rooms/upload")
    @PreAuthorize("hasAnyAuthority(\"" + AuthoritiesConstants.ADMIN + "\" , \"" + AuthoritiesConstants.SUPER_ADMIN
        + "\" , \"" + AuthoritiesConstants.BROKER + "\")")
    public void uploadImage(@ModelAttribute ChatRoomImageDTO chatRoomImageDTO) {
        log.info("REST request to upload User image : {}", chatRoomImageDTO.getFile().getOriginalFilename());
        chatRoomService.uploadChatRoomImage(chatRoomImageDTO,false);
    }

    @GetMapping("/chat-rooms")
    public ResponseEntity<List<ChatRoom>> getAllChatRooms(
            ChatRoomPrimaryCriteria criteria,
            @org.springdoc.api.annotations.ParameterObject Pageable pageable) {
        log.info("REST request to get ChatRooms by criteria: {}", criteria);
        Page<ChatRoom> page = chatRoomPrimaryQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/chat-rooms/count")
    public ResponseEntity<Long> countChatRooms(ChatRoomCriteria criteria) {
        log.info("REST request to count ChatRooms by criteria: {}", criteria);
        return ResponseEntity.ok().body(chatRoomQueryService.countByCriteria(criteria));
    }

    @GetMapping("/chat-rooms/{id}")
    public ResponseEntity<ChatRoom> getChatRoom(@PathVariable Long id) {
        log.info("REST request to get ChatRoom : {}", id);
        Optional<ChatRoom> chatRoom = chatRoomService.findOne(id);
        return ResponseUtil.wrapOrNotFound(chatRoom);
    }

    @DeleteMapping("/chat-rooms/{id}")
    public ResponseEntity<ChatRoom> deleteChatRoom(@PathVariable Long id) {
        log.info("REST request to delete ChatRoom : {}", id);
        ChatRoom chatRoom = chatRoomService.findOne(id).orElse(null);
        if (chatRoom.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, chatRoom.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!chatRoomRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        chatRoom.setUpdatedAt(ZonedDateTime.now());
        chatRoom.action(ActionEnum.DELETE);
        chatRoom.status(StatusEnum.PENDING);
        ChatRoom result = chatRoomService.update(chatRoom);
        String message = "A chatRoom is deleted with identifier " + result.getId()
                + ". Chat room will be deleted after administrator approval";
        HttpHeaders headers = new HttpHeaders();
        String encodedMessage = URLEncoder.encode(
                message,
                StandardCharsets.UTF_8);
        headers.add("X-" + applicationName + "-alert", encodedMessage);
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(result);
    }

    @PostMapping("/chat-rooms/{id}/approve")
    public ResponseEntity<ChatRoom> approveChatRoom(@PathVariable Long id) {
        log.info("REST request to approve ChatRoom : {}", id);
        String name = SecurityUtils.getCurrentUserLogin().orElse(null);
        ChatRoom chatRoom = chatRoomService.findOne(id).orElse(null);
        if (brokerRepository.findByUsernameAndStatusIsFalse(chatRoom.getBrokerName()).isPresent()) {
            throw new BadRequestAlertException("Chat room can not be approved because broker account is deactivated",
                    ENTITY_NAME, "idinvalid");
        }
        if (chatRoom.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, chatRoom.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!chatRoomRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        ChatRoom result = null;
        switch (chatRoom.getAction()) {
            case CREATE:
                result = chatRoomService.approveCreateOrUpdate(chatRoom, name);
                return ResponseEntity
                        .ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME,
                                chatRoom.getId().toString()))
                        .body(result);
            case UPDATE:
                result = chatRoomService.approveCreateOrUpdate(chatRoom, name);
                return ResponseEntity
                        .ok()
                        .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME,
                                chatRoom.getId().toString()))
                        .body(result);
            case DELETE:
                chatRoomService.approveDelete(chatRoom, name);
                return ResponseEntity
                        .ok()
                        .headers(HeaderUtil.createAlert(applicationName,
                                "A Chat Room is approve with identifier " + id.toString(),
                                chatRoom.getId().toString()))
                        .body(result);
            default:
                throw new BadRequestAlertException("Invalid action", ENTITY_NAME, "actioninvalid");
        }
    }

    @PostMapping(path = "/chat-rooms/{id}/reject")
    public ResponseEntity<ChatRoom> rejectChatRoom(@PathVariable Long id, @RequestBody ChatRoom chatRoom) {
        log.info("REST request to reject ChatRoom : {} , {}", id, chatRoom);
        String name = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (chatRoom.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, chatRoom.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!chatRoomRepository.existsById(Long.valueOf(id))) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }
        ChatRoom result = chatRoomService.rejectChatRoom(chatRoom, name);
        HttpHeaders headers = new HttpHeaders();
        String encodedMessage = URLEncoder.encode("A Chat Room is rejected with identifier " + id.toString() +
                " and reason " + chatRoom.getRejectReason(), StandardCharsets.UTF_8);

        headers.add("X-" + applicationName + "-alert", encodedMessage);
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(result);
    }
}
