package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.service.dto.CopySubscriberDetailsDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopySubscriberDetails}.
 */
public interface CopySubscriberDetailsService {
    /**
     * Save a copySubscriberDetails.
     *
     * @param copySubscriberDetailsDTO the entity to save.
     * @return the persisted entity.
     */
    CopySubscriberDetailsDTO save(CopySubscriberDetailsDTO copySubscriberDetailsDTO);

    /**
     * Updates a copySubscriberDetails.
     *
     * @param copySubscriberDetailsDTO the entity to update.
     * @return the persisted entity.
     */
    CopySubscriberDetailsDTO update(CopySubscriberDetailsDTO copySubscriberDetailsDTO);

    /**
     * Partially updates a copySubscriberDetails.
     *
     * @param copySubscriberDetailsDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CopySubscriberDetailsDTO> partialUpdate(CopySubscriberDetailsDTO copySubscriberDetailsDTO);

    /**
     * Get all the copySubscriberDetails.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopySubscriberDetailsDTO> findAll(Pageable pageable);

    /**
     * Get the "id" copySubscriberDetails.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CopySubscriberDetailsDTO> findOne(Long id);

    /**
     * Delete the "id" copySubscriberDetails.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
