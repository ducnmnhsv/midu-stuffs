package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.ekycadmin.service.dto.EContractDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.techx.tradex.ekycadmin.domain.EContract}.
 */
public interface EContractService {
    /**
     * Save a eContract.
     *
     * @param eContractDTO the entity to save.
     * @return the persisted entity.
     */
    EContractDTO save(EContractDTO eContractDTO);

    /**
     * Partially updates a eContract.
     *
     * @param eContractDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<EContractDTO> partialUpdate(EContractDTO eContractDTO);

    /**
     * Get all the eContracts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<EContractDTO> findAll(Pageable pageable);

    /**
     * Get the "id" eContract.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<EContractDTO> findOne(Long id);

    /**
     * Delete the "id" eContract.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
