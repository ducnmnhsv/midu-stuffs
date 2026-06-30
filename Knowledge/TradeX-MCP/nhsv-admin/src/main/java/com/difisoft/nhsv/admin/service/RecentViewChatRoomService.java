package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.RecentViewChatRoom;
import com.difisoft.nhsv.admin.repository.RecentViewChatRoomRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link RecentViewChatRoom}.
 */
@Service
@Transactional
public class RecentViewChatRoomService {

    private final Logger log = LoggerFactory.getLogger(RecentViewChatRoomService.class);

    private final RecentViewChatRoomRepository recentViewChatRoomRepository;

    public RecentViewChatRoomService(RecentViewChatRoomRepository recentViewChatRoomRepository) {
        this.recentViewChatRoomRepository = recentViewChatRoomRepository;
    }

    /**
     * Save a recentViewChatRoom.
     *
     * @param recentViewChatRoom the entity to save.
     * @return the persisted entity.
     */
    public RecentViewChatRoom save(RecentViewChatRoom recentViewChatRoom) {
        log.debug("Request to save RecentViewChatRoom : {}", recentViewChatRoom);
        return recentViewChatRoomRepository.save(recentViewChatRoom);
    }

    /**
     * Update a recentViewChatRoom.
     *
     * @param recentViewChatRoom the entity to save.
     * @return the persisted entity.
     */
    public RecentViewChatRoom update(RecentViewChatRoom recentViewChatRoom) {
        log.debug("Request to update RecentViewChatRoom : {}", recentViewChatRoom);
        return recentViewChatRoomRepository.save(recentViewChatRoom);
    }

    /**
     * Partially update a recentViewChatRoom.
     *
     * @param recentViewChatRoom the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<RecentViewChatRoom> partialUpdate(RecentViewChatRoom recentViewChatRoom) {
        log.debug("Request to partially update RecentViewChatRoom : {}", recentViewChatRoom);

        return recentViewChatRoomRepository
            .findById(recentViewChatRoom.getId())
            .map(existingRecentViewChatRoom -> {
                if (recentViewChatRoom.getUserId() != null) {
                    existingRecentViewChatRoom.setUserId(recentViewChatRoom.getUserId());
                }
                if (recentViewChatRoom.getChatRoomId() != null) {
                    existingRecentViewChatRoom.setChatRoomId(recentViewChatRoom.getChatRoomId());
                }
                if (recentViewChatRoom.getCreatedAt() != null) {
                    existingRecentViewChatRoom.setCreatedAt(recentViewChatRoom.getCreatedAt());
                }
                if (recentViewChatRoom.getUpdatedAt() != null) {
                    existingRecentViewChatRoom.setUpdatedAt(recentViewChatRoom.getUpdatedAt());
                }
                if (recentViewChatRoom.getDeletedAt() != null) {
                    existingRecentViewChatRoom.setDeletedAt(recentViewChatRoom.getDeletedAt());
                }

                return existingRecentViewChatRoom;
            })
            .map(recentViewChatRoomRepository::save);
    }

    /**
     * Get all the recentViewChatRooms.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<RecentViewChatRoom> findAll() {
        log.debug("Request to get all RecentViewChatRooms");
        return recentViewChatRoomRepository.findAll();
    }

    /**
     * Get one recentViewChatRoom by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<RecentViewChatRoom> findOne(Long id) {
        log.debug("Request to get RecentViewChatRoom : {}", id);
        return recentViewChatRoomRepository.findById(id);
    }

    /**
     * Delete the recentViewChatRoom by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete RecentViewChatRoom : {}", id);
        recentViewChatRoomRepository.deleteById(id);
    }
}
