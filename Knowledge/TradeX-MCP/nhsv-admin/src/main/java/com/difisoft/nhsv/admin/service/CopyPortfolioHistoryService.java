package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.service.dto.CopyPortfolioHistoryDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyPortfolioHistory}.
 */
public interface CopyPortfolioHistoryService {
    /**
     * Save a copyPortfolioHistory.
     *
     * @param copyPortfolioHistoryDTO the entity to save.
     * @return the persisted entity.
     */
    CopyPortfolioHistoryDTO save(CopyPortfolioHistoryDTO copyPortfolioHistoryDTO);

    /**
     * Updates a copyPortfolioHistory.
     *
     * @param copyPortfolioHistoryDTO the entity to update.
     * @return the persisted entity.
     */
    CopyPortfolioHistoryDTO update(CopyPortfolioHistoryDTO copyPortfolioHistoryDTO);

    /**
     * Partially updates a copyPortfolioHistory.
     *
     * @param copyPortfolioHistoryDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CopyPortfolioHistoryDTO> partialUpdate(CopyPortfolioHistoryDTO copyPortfolioHistoryDTO);

    /**
     * Get all the copyPortfolioHistories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopyPortfolioHistoryDTO> findAll(Pageable pageable);

    /**
     * Get all the copyPortfolioHistories with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopyPortfolioHistoryDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" copyPortfolioHistory.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CopyPortfolioHistoryDTO> findOne(Long id);

    /**
     * Delete the "id" copyPortfolioHistory.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
