package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderDetailsDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyMarketLeaderDetails}.
 */
public interface CopyMarketLeaderDetailsService {
    /**
     * Save a copyMarketLeaderDetails.
     *
     * @param copyMarketLeaderDetailsDTO the entity to save.
     * @return the persisted entity.
     */
    CopyMarketLeaderDetailsDTO save(CopyMarketLeaderDetailsDTO copyMarketLeaderDetailsDTO);

    /**
     * Updates a copyMarketLeaderDetails.
     *
     * @param copyMarketLeaderDetailsDTO the entity to update.
     * @return the persisted entity.
     */
    CopyMarketLeaderDetailsDTO update(CopyMarketLeaderDetailsDTO copyMarketLeaderDetailsDTO);

    /**
     * Partially updates a copyMarketLeaderDetails.
     *
     * @param copyMarketLeaderDetailsDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CopyMarketLeaderDetailsDTO> partialUpdate(CopyMarketLeaderDetailsDTO copyMarketLeaderDetailsDTO);

    /**
     * Get all the copyMarketLeaderDetails.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopyMarketLeaderDetailsDTO> findAll(Pageable pageable);

    /**
     * Get all the copyMarketLeaderDetails with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopyMarketLeaderDetailsDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" copyMarketLeaderDetails.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CopyMarketLeaderDetailsDTO> findOne(Long id);

    /**
     * Delete the "id" copyMarketLeaderDetails.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
