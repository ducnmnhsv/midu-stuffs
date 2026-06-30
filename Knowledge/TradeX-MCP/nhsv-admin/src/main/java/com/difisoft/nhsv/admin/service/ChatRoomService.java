package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.ChatRoom;
import com.difisoft.nhsv.admin.repository.ChatRoomRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ChatRoom}.
 */
@Service
@Transactional
public class ChatRoomService {

    private final Logger log = LoggerFactory.getLogger(ChatRoomService.class);

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    /**
     * Save a chatRoom.
     *
     * @param chatRoom the entity to save.
     * @return the persisted entity.
     */
    public ChatRoom save(ChatRoom chatRoom) {
        log.debug("Request to save ChatRoom : {}", chatRoom);
        return chatRoomRepository.save(chatRoom);
    }

    /**
     * Update a chatRoom.
     *
     * @param chatRoom the entity to save.
     * @return the persisted entity.
     */
    public ChatRoom update(ChatRoom chatRoom) {
        log.debug("Request to update ChatRoom : {}", chatRoom);
        return chatRoomRepository.save(chatRoom);
    }

    /**
     * Partially update a chatRoom.
     *
     * @param chatRoom the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ChatRoom> partialUpdate(ChatRoom chatRoom) {
        log.debug("Request to partially update ChatRoom : {}", chatRoom);

        return chatRoomRepository
            .findById(chatRoom.getId())
            .map(existingChatRoom -> {
                if (chatRoom.getGroupName() != null) {
                    existingChatRoom.setGroupName(chatRoom.getGroupName());
                }
                if (chatRoom.getGroupOwner() != null) {
                    existingChatRoom.setGroupOwner(chatRoom.getGroupOwner());
                }
                if (chatRoom.getIntroduction() != null) {
                    existingChatRoom.setIntroduction(chatRoom.getIntroduction());
                }
                if (chatRoom.getPhoto() != null) {
                    existingChatRoom.setPhoto(chatRoom.getPhoto());
                }
                if (chatRoom.getBrokerName() != null) {
                    existingChatRoom.setBrokerName(chatRoom.getBrokerName());
                }
                if (chatRoom.getBrokerContact() != null) {
                    existingChatRoom.setBrokerContact(chatRoom.getBrokerContact());
                }
                if (chatRoom.getStatus() != null) {
                    existingChatRoom.setStatus(chatRoom.getStatus());
                }
                if (chatRoom.getCreatedBy() != null) {
                    existingChatRoom.setCreatedBy(chatRoom.getCreatedBy());
                }
                if (chatRoom.getCreatedAt() != null) {
                    existingChatRoom.setCreatedAt(chatRoom.getCreatedAt());
                }
                if (chatRoom.getUpdatedAt() != null) {
                    existingChatRoom.setUpdatedAt(chatRoom.getUpdatedAt());
                }
                if (chatRoom.getApprovedAt() != null) {
                    existingChatRoom.setApprovedAt(chatRoom.getApprovedAt());
                }
                if (chatRoom.getRejectedAt() != null) {
                    existingChatRoom.setRejectedAt(chatRoom.getRejectedAt());
                }
                if (chatRoom.getRejectReason() != null) {
                    existingChatRoom.setRejectReason(chatRoom.getRejectReason());
                }
                if (chatRoom.getApprovedBy() != null) {
                    existingChatRoom.setApprovedBy(chatRoom.getApprovedBy());
                }
                if (chatRoom.getRejectedBy() != null) {
                    existingChatRoom.setRejectedBy(chatRoom.getRejectedBy());
                }
                if (chatRoom.getAction() != null) {
                    existingChatRoom.setAction(chatRoom.getAction());
                }

                return existingChatRoom;
            })
            .map(chatRoomRepository::save);
    }

    /**
     * Get all the chatRooms.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ChatRoom> findAll(Pageable pageable) {
        log.debug("Request to get all ChatRooms");
        return chatRoomRepository.findAll(pageable);
    }

    /**
     * Get one chatRoom by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ChatRoom> findOne(Long id) {
        log.debug("Request to get ChatRoom : {}", id);
        return chatRoomRepository.findById(id);
    }

    /**
     * Delete the chatRoom by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete ChatRoom : {}", id);
        chatRoomRepository.deleteById(id);
    }
}
