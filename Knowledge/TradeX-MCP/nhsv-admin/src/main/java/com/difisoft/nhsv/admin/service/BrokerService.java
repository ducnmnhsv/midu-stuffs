package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.Broker;
import com.difisoft.nhsv.admin.repository.BrokerRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Broker}.
 */
@Service
@Transactional
public class BrokerService {

    private final Logger log = LoggerFactory.getLogger(BrokerService.class);

    private final BrokerRepository brokerRepository;

    public BrokerService(BrokerRepository brokerRepository) {
        this.brokerRepository = brokerRepository;
    }

    /**
     * Save a broker.
     *
     * @param broker the entity to save.
     * @return the persisted entity.
     */
    public Broker save(Broker broker) {
        log.debug("Request to save Broker : {}", broker);
        return brokerRepository.save(broker);
    }

    /**
     * Update a broker.
     *
     * @param broker the entity to save.
     * @return the persisted entity.
     */
    public Broker update(Broker broker) {
        log.debug("Request to update Broker : {}", broker);
        broker.setIsPersisted();
        return brokerRepository.save(broker);
    }

    /**
     * Partially update a broker.
     *
     * @param broker the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<Broker> partialUpdate(Broker broker) {
        log.debug("Request to partially update Broker : {}", broker);

        return brokerRepository
            .findById(broker.getId())
            .map(existingBroker -> {
                if (broker.getUsername() != null) {
                    existingBroker.setUsername(broker.getUsername());
                }
                if (broker.getFullname() != null) {
                    existingBroker.setFullname(broker.getFullname());
                }
                if (broker.getStatus() != null) {
                    existingBroker.setStatus(broker.getStatus());
                }
                if (broker.getTotalChatRoom() != null) {
                    existingBroker.setTotalChatRoom(broker.getTotalChatRoom());
                }
                if (broker.getCurrentRank() != null) {
                    existingBroker.setCurrentRank(broker.getCurrentRank());
                }
                if (broker.getIsDynamic() != null) {
                    existingBroker.setIsDynamic(broker.getIsDynamic());
                }
                if (broker.getEmail() != null) {
                    existingBroker.setEmail(broker.getEmail());
                }
                if (broker.getCreatedAt() != null) {
                    existingBroker.setCreatedAt(broker.getCreatedAt());
                }
                if (broker.getUpdatedAt() != null) {
                    existingBroker.setUpdatedAt(broker.getUpdatedAt());
                }
                if (broker.getDeactivatedAt() != null) {
                    existingBroker.setDeactivatedAt(broker.getDeactivatedAt());
                }
                if (broker.getDeactivatedBy() != null) {
                    existingBroker.setDeactivatedBy(broker.getDeactivatedBy());
                }
                if (broker.getInvitedBy() != null) {
                    existingBroker.setInvitedBy(broker.getInvitedBy());
                }

                return existingBroker;
            })
            .map(brokerRepository::save);
    }

    /**
     * Get all the brokers.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Broker> findAll() {
        log.debug("Request to get all Brokers");
        return brokerRepository.findAll();
    }

    /**
     * Get one broker by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Broker> findOne(Long id) {
        log.debug("Request to get Broker : {}", id);
        return brokerRepository.findById(id);
    }

    /**
     * Delete the broker by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Broker : {}", id);
        brokerRepository.deleteById(id);
    }
}
