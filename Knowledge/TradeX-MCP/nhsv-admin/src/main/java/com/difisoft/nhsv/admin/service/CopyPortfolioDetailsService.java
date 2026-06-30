package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailsDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyPortfolioDetails}.
 */
public interface CopyPortfolioDetailsService {
    /**
     * Save a copyPortfolioDetails.
     *
     * @param copyPortfolioDetailsDTO the entity to save.
     * @return the persisted entity.
     */
    CopyPortfolioDetailsDTO save(CopyPortfolioDetailsDTO copyPortfolioDetailsDTO);

    /**
     * Updates a copyPortfolioDetails.
     *
     * @param copyPortfolioDetailsDTO the entity to update.
     * @return the persisted entity.
     */
    CopyPortfolioDetailsDTO update(CopyPortfolioDetailsDTO copyPortfolioDetailsDTO);

    /**
     * Partially updates a copyPortfolioDetails.
     *
     * @param copyPortfolioDetailsDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CopyPortfolioDetailsDTO> partialUpdate(CopyPortfolioDetailsDTO copyPortfolioDetailsDTO);

    /**
     * Get all the copyPortfolioDetails.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopyPortfolioDetailsDTO> findAll(Pageable pageable);

    /**
     * Get the "id" copyPortfolioDetails.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CopyPortfolioDetailsDTO> findOne(Long id);

    /**
     * Delete the "id" copyPortfolioDetails.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
