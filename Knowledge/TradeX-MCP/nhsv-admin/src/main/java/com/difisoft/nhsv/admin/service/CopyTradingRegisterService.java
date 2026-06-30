package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.service.dto.CopyTradingRegisterDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyTradingRegister}.
 */
public interface CopyTradingRegisterService {
    /**
     * Save a copyTradingRegister.
     *
     * @param copyTradingRegisterDTO the entity to save.
     * @return the persisted entity.
     */
    CopyTradingRegisterDTO save(CopyTradingRegisterDTO copyTradingRegisterDTO);

    /**
     * Updates a copyTradingRegister.
     *
     * @param copyTradingRegisterDTO the entity to update.
     * @return the persisted entity.
     */
    CopyTradingRegisterDTO update(CopyTradingRegisterDTO copyTradingRegisterDTO);

    /**
     * Partially updates a copyTradingRegister.
     *
     * @param copyTradingRegisterDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CopyTradingRegisterDTO> partialUpdate(CopyTradingRegisterDTO copyTradingRegisterDTO);

    /**
     * Get all the copyTradingRegisters.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopyTradingRegisterDTO> findAll(Pageable pageable);

    /**
     * Get the "id" copyTradingRegister.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CopyTradingRegisterDTO> findOne(Long id);

    /**
     * Delete the "id" copyTradingRegister.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
