package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailHistoryDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyPortfolioDetailHistory}.
 */
public interface CopyPortfolioDetailHistoryService {
    /**
     * Save a copyPortfolioDetailHistory.
     *
     * @param copyPortfolioDetailHistoryDTO the entity to save.
     * @return the persisted entity.
     */
    CopyPortfolioDetailHistoryDTO save(CopyPortfolioDetailHistoryDTO copyPortfolioDetailHistoryDTO);

    /**
     * Updates a copyPortfolioDetailHistory.
     *
     * @param copyPortfolioDetailHistoryDTO the entity to update.
     * @return the persisted entity.
     */
    CopyPortfolioDetailHistoryDTO update(CopyPortfolioDetailHistoryDTO copyPortfolioDetailHistoryDTO);

    /**
     * Partially updates a copyPortfolioDetailHistory.
     *
     * @param copyPortfolioDetailHistoryDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CopyPortfolioDetailHistoryDTO> partialUpdate(CopyPortfolioDetailHistoryDTO copyPortfolioDetailHistoryDTO);

    /**
     * Get all the copyPortfolioDetailHistories.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopyPortfolioDetailHistoryDTO> findAll(Pageable pageable);

    /**
     * Get the "id" copyPortfolioDetailHistory.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CopyPortfolioDetailHistoryDTO> findOne(Long id);

    /**
     * Delete the "id" copyPortfolioDetailHistory.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
