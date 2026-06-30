package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.service.dto.CopyTradingOrderDTO;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.difisoft.nhsv.admin.domain.CopyTradingOrder}.
 */
public interface CopyTradingOrderService {
    /**
     * Save a copyTradingOrder.
     *
     * @param copyTradingOrderDTO the entity to save.
     * @return the persisted entity.
     */
    CopyTradingOrderDTO save(CopyTradingOrderDTO copyTradingOrderDTO);

    /**
     * Updates a copyTradingOrder.
     *
     * @param copyTradingOrderDTO the entity to update.
     * @return the persisted entity.
     */
    CopyTradingOrderDTO update(CopyTradingOrderDTO copyTradingOrderDTO);

    /**
     * Partially updates a copyTradingOrder.
     *
     * @param copyTradingOrderDTO the entity to update partially.
     * @return the persisted entity.
     */
    Optional<CopyTradingOrderDTO> partialUpdate(CopyTradingOrderDTO copyTradingOrderDTO);

    /**
     * Get all the copyTradingOrders.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<CopyTradingOrderDTO> findAll(Pageable pageable);

    /**
     * Get the "id" copyTradingOrder.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<CopyTradingOrderDTO> findOne(Long id);

    /**
     * Delete the "id" copyTradingOrder.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
