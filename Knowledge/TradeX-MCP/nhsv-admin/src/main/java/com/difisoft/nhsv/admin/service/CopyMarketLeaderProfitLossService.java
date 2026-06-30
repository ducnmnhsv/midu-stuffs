package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderProfitLossDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLoss}.
 */
public interface CopyMarketLeaderProfitLossService {
    /**
     * Save a copyMarketLeaderProfitLoss.
     *
     * @param copyMarketLeaderProfitLossDTO the entity to save.
     * @return the persisted entity.
     */
    CopyMarketLeaderProfitLossDTO save(CopyMarketLeaderProfitLossDTO copyMarketLeaderProfitLossDTO);

    /**
     * Updates a copyMarketLeaderProfitLoss.
     *
     * @param copyMarketLeaderProfitLossDTO the entity to update.
     * @return the persisted entity.
     */
    CopyMarketLeaderProfitLossDTO update(CopyMarketLeaderProfitLossDTO copyMarketLeaderProfitLossDTO);

    /**
     * Partially updates a copyMarketLeaderProfitLoss.
     *
     * @param copyMarketLeaderProfitLossDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CopyMarketLeaderProfitLossDTO> partialUpdate(CopyMarketLeaderProfitLossDTO copyMarketLeaderProfitLossDTO);

    /**
     * Get all the copyMarketLeaderProfitLosses.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopyMarketLeaderProfitLossDTO> findAll(Pageable pageable);

    /**
     * Get the "id" copyMarketLeaderProfitLoss.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CopyMarketLeaderProfitLossDTO> findOne(Long id);

    /**
     * Delete the "id" copyMarketLeaderProfitLoss.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
