package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.service.dto.CopySubscriberHistoryDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopySubscriberHistory}.
 */
public interface CopySubscriberHistoryService {
    /**
     * Save a copySubscriberHistory.
     *
     * @param copySubscriberHistoryDTO the entity to save.
     * @return the persisted entity.
     */
    CopySubscriberHistoryDTO save(CopySubscriberHistoryDTO copySubscriberHistoryDTO);

    /**
     * Updates a copySubscriberHistory.
     *
     * @param copySubscriberHistoryDTO the entity to update.
     * @return the persisted entity.
     */
    CopySubscriberHistoryDTO update(CopySubscriberHistoryDTO copySubscriberHistoryDTO);

    /**
     * Partially updates a copySubscriberHistory.
     *
     * @param copySubscriberHistoryDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CopySubscriberHistoryDTO> partialUpdate(CopySubscriberHistoryDTO copySubscriberHistoryDTO);

    /**
     * Get all the copySubscriberHistories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopySubscriberHistoryDTO> findAll(Pageable pageable);

    /**
     * Get all the copySubscriberHistories with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopySubscriberHistoryDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" copySubscriberHistory.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CopySubscriberHistoryDTO> findOne(Long id);

    /**
     * Delete the "id" copySubscriberHistory.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
