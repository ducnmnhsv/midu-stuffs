package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyPortfolio}.
 */
public interface CopyPortfolioService {
    /**
     * Save a copyPortfolio.
     *
     * @param copyPortfolioDTO the entity to save.
     * @return the persisted entity.
     */
    CopyPortfolioDTO save(CopyPortfolioDTO copyPortfolioDTO);

    /**
     * Updates a copyPortfolio.
     *
     * @param copyPortfolioDTO the entity to update.
     * @return the persisted entity.
     */
    CopyPortfolioDTO update(CopyPortfolioDTO copyPortfolioDTO);

    /**
     * Partially updates a copyPortfolio.
     *
     * @param copyPortfolioDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CopyPortfolioDTO> partialUpdate(CopyPortfolioDTO copyPortfolioDTO);

    /**
     * Get all the copyPortfolios.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopyPortfolioDTO> findAll(Pageable pageable);

    /**
     * Get all the copyPortfolios with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopyPortfolioDTO> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" copyPortfolio.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CopyPortfolioDTO> findOne(Long id);

    /**
     * Delete the "id" copyPortfolio.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
