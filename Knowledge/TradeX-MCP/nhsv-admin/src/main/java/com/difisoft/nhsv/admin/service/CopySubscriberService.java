package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.service.dto.CopySubscriberDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopySubscriber}.
 */
public interface CopySubscriberService {
    /**
     * Save a copySubscriber.
     *
     * @param copySubscriberDTO the entity to save.
     * @return the persisted entity.
     */
    CopySubscriberDTO save(CopySubscriberDTO copySubscriberDTO);

    /**
     * Updates a copySubscriber.
     *
     * @param copySubscriberDTO the entity to update.
     * @return the persisted entity.
     */
    CopySubscriberDTO update(CopySubscriberDTO copySubscriberDTO);

    /**
     * Partially updates a copySubscriber.
     *
     * @param copySubscriberDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CopySubscriberDTO> partialUpdate(CopySubscriberDTO copySubscriberDTO);

    /**
     * Get all the copySubscribers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopySubscriberDTO> findAll(Pageable pageable);

    /**
     * Get all the copySubscribers with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopySubscriberDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" copySubscriber.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CopySubscriberDTO> findOne(Long id);

    /**
     * Delete the "id" copySubscriber.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
