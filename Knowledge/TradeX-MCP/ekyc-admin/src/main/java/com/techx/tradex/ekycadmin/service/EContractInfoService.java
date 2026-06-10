package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.ekycadmin.service.dto.EContractInfoDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.techx.tradex.ekycadmin.domain.EContractInfo}.
 */
public interface EContractInfoService {
    /**
     * Save a eContractInfo.
     *
     * @param eContractInfoDTO the entity to save.
     * @return the persisted entity.
     */
    EContractInfoDTO save(EContractInfoDTO eContractInfoDTO);

    /**
     * Partially updates a eContractInfo.
     *
     * @param eContractInfoDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<EContractInfoDTO> partialUpdate(EContractInfoDTO eContractInfoDTO);

    /**
     * Get all the eContractInfos.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<EContractInfoDTO> findAll(Pageable pageable);

    /**
     * Get the "id" eContractInfo.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<EContractInfoDTO> findOne(Long id);

    /**
     * Delete the "id" eContractInfo.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
