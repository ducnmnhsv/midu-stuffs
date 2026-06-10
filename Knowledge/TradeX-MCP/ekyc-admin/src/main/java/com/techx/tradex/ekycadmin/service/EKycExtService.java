package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.ekycadmin.domain.EKycExt;
import com.techx.tradex.ekycadmin.repository.EKycExtRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link EKycExt}.
 */
@Service
@Transactional
public class EKycExtService {

    private final Logger log = LoggerFactory.getLogger(EKycExtService.class);

    private final EKycExtRepository eKycExtRepository;

    public EKycExtService(EKycExtRepository eKycExtRepository) {
        this.eKycExtRepository = eKycExtRepository;
    }

    /**
     * Save a eKycExt.
     *
     * @param eKycExt the entity to save.
     * @return the persisted entity.
     */
    public EKycExt save(EKycExt eKycExt) {
        log.debug("Request to save EKycExt : {}", eKycExt);
        return eKycExtRepository.save(eKycExt);
    }

    /**
     * Partially update a eKycExt.
     *
     * @param eKycExt the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<EKycExt> partialUpdate(EKycExt eKycExt) {
        log.debug("Request to partially update EKycExt : {}", eKycExt);

        return eKycExtRepository
            .findById(eKycExt.getId())
            .map(
                existingEKycExt -> {
                    if (eKycExt.getLogId() != null) {
                        existingEKycExt.setLogId(eKycExt.getLogId());
                    }
                    if (eKycExt.getRawData() != null) {
                        existingEKycExt.setRawData(eKycExt.getRawData());
                    }

                    return existingEKycExt;
                }
            )
            .map(eKycExtRepository::save);
    }

    /**
     * Get all the eKycExts.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<EKycExt> findAll() {
        log.debug("Request to get all EKycExts");
        return eKycExtRepository.findAll();
    }

    /**
     * Get one eKycExt by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<EKycExt> findOne(Long id) {
        log.debug("Request to get EKycExt : {}", id);
        return eKycExtRepository.findById(id);
    }

    /**
     * Delete the eKycExt by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete EKycExt : {}", id);
        eKycExtRepository.deleteById(id);
    }
}
