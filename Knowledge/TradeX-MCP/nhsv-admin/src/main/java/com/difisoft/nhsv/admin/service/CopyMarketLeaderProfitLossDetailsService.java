package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderProfitLossDetailsDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLossDetails}.
 */
public interface CopyMarketLeaderProfitLossDetailsService {
    /**
     * Save a copyMarketLeaderProfitLossDetails.
     *
     * @param copyMarketLeaderProfitLossDetailsDTO the entity to save.
     * @return the persisted entity.
     */
    CopyMarketLeaderProfitLossDetailsDTO save(CopyMarketLeaderProfitLossDetailsDTO copyMarketLeaderProfitLossDetailsDTO);

    /**
     * Updates a copyMarketLeaderProfitLossDetails.
     *
     * @param copyMarketLeaderProfitLossDetailsDTO the entity to update.
     * @return the persisted entity.
     */
    CopyMarketLeaderProfitLossDetailsDTO update(CopyMarketLeaderProfitLossDetailsDTO copyMarketLeaderProfitLossDetailsDTO);

    /**
     * Partially updates a copyMarketLeaderProfitLossDetails.
     *
     * @param copyMarketLeaderProfitLossDetailsDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CopyMarketLeaderProfitLossDetailsDTO> partialUpdate(CopyMarketLeaderProfitLossDetailsDTO copyMarketLeaderProfitLossDetailsDTO);

    /**
     * Get all the copyMarketLeaderProfitLossDetails.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopyMarketLeaderProfitLossDetailsDTO> findAll(Pageable pageable);

    /**
     * Get the "id" copyMarketLeaderProfitLossDetails.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CopyMarketLeaderProfitLossDetailsDTO> findOne(Long id);

    /**
     * Delete the "id" copyMarketLeaderProfitLossDetails.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
