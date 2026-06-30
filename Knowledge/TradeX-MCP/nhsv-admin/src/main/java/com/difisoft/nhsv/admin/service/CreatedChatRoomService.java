package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.CreatedChatRoom;
import com.difisoft.nhsv.admin.repository.CreatedChatRoomRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link CreatedChatRoom}.
 */
@Service
@Transactional
public class CreatedChatRoomService {

    private final Logger log = LoggerFactory.getLogger(CreatedChatRoomService.class);

    private final CreatedChatRoomRepository createdChatRoomRepository;

    public CreatedChatRoomService(CreatedChatRoomRepository createdChatRoomRepository) {
        this.createdChatRoomRepository = createdChatRoomRepository;
    }

    /**
     * Save a createdChatRoom.
     *
     * @param createdChatRoom the entity to save.
     * @return the persisted entity.
     */
    public CreatedChatRoom save(CreatedChatRoom createdChatRoom) {
        log.debug("Request to save CreatedChatRoom : {}", createdChatRoom);
        return createdChatRoomRepository.save(createdChatRoom);
    }

    /**
     * Update a createdChatRoom.
     *
     * @param createdChatRoom the entity to save.
     * @return the persisted entity.
     */
    public CreatedChatRoom update(CreatedChatRoom createdChatRoom) {
        log.debug("Request to update CreatedChatRoom : {}", createdChatRoom);
        return createdChatRoomRepository.save(createdChatRoom);
    }

    /**
     * Partially update a createdChatRoom.
     *
     * @param createdChatRoom the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CreatedChatRoom> partialUpdate(CreatedChatRoom createdChatRoom) {
        log.debug("Request to partially update CreatedChatRoom : {}", createdChatRoom);

        return createdChatRoomRepository
            .findById(createdChatRoom.getId())
            .map(existingCreatedChatRoom -> {
                if (createdChatRoom.getGroupName() != null) {
                    existingCreatedChatRoom.setGroupName(createdChatRoom.getGroupName());
                }
                if (createdChatRoom.getGroupOwner() != null) {
                    existingCreatedChatRoom.setGroupOwner(createdChatRoom.getGroupOwner());
                }
                if (createdChatRoom.getIntroduction() != null) {
                    existingCreatedChatRoom.setIntroduction(createdChatRoom.getIntroduction());
                }
                if (createdChatRoom.getPhoto() != null) {
                    existingCreatedChatRoom.setPhoto(createdChatRoom.getPhoto());
                }
                if (createdChatRoom.getBrokerName() != null) {
                    existingCreatedChatRoom.setBrokerName(createdChatRoom.getBrokerName());
                }
                if (createdChatRoom.getBrokerContact() != null) {
                    existingCreatedChatRoom.setBrokerContact(createdChatRoom.getBrokerContact());
                }
                if (createdChatRoom.getStatus() != null) {
                    existingCreatedChatRoom.setStatus(createdChatRoom.getStatus());
                }
                if (createdChatRoom.getCreatedBy() != null) {
                    existingCreatedChatRoom.setCreatedBy(createdChatRoom.getCreatedBy());
                }
                if (createdChatRoom.getCreatedAt() != null) {
                    existingCreatedChatRoom.setCreatedAt(createdChatRoom.getCreatedAt());
                }
                if (createdChatRoom.getUpdatedAt() != null) {
                    existingCreatedChatRoom.setUpdatedAt(createdChatRoom.getUpdatedAt());
                }
                if (createdChatRoom.getApprovedAt() != null) {
                    existingCreatedChatRoom.setApprovedAt(createdChatRoom.getApprovedAt());
                }
                if (createdChatRoom.getApprovedBy() != null) {
                    existingCreatedChatRoom.setApprovedBy(createdChatRoom.getApprovedBy());
                }
                if (createdChatRoom.getRejectReason() != null) {
                    existingCreatedChatRoom.setRejectReason(createdChatRoom.getRejectReason());
                }
                if (createdChatRoom.getTotalView() != null) {
                    existingCreatedChatRoom.setTotalView(createdChatRoom.getTotalView());
                }

                return existingCreatedChatRoom;
            })
            .map(createdChatRoomRepository::save);
    }

    /**
     * Get all the createdChatRooms.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<CreatedChatRoom> findAll(Pageable pageable) {
        log.debug("Request to get all CreatedChatRooms");
        return createdChatRoomRepository.findAll(pageable);
    }

    /**
     * Get one createdChatRoom by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CreatedChatRoom> findOne(Long id) {
        log.debug("Request to get CreatedChatRoom : {}", id);
        return createdChatRoomRepository.findById(id);
    }

    /**
     * Delete the createdChatRoom by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete CreatedChatRoom : {}", id);
        createdChatRoomRepository.deleteById(id);
    }
}
