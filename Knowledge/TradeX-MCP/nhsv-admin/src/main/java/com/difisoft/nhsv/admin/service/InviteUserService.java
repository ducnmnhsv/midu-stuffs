package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.InviteUser;
import com.difisoft.nhsv.admin.repository.InviteUserRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link InviteUser}.
 */
@Service
@Transactional
public class InviteUserService {

    private final Logger log = LoggerFactory.getLogger(InviteUserService.class);

    private final InviteUserRepository inviteUserRepository;

    public InviteUserService(InviteUserRepository inviteUserRepository) {
        this.inviteUserRepository = inviteUserRepository;
    }

    /**
     * Save a inviteUser.
     *
     * @param inviteUser the entity to save.
     * @return the persisted entity.
     */
    public InviteUser save(InviteUser inviteUser) {
        log.debug("Request to save InviteUser : {}", inviteUser);
        return inviteUserRepository.save(inviteUser);
    }

    /**
     * Update a inviteUser.
     *
     * @param inviteUser the entity to save.
     * @return the persisted entity.
     */
    public InviteUser update(InviteUser inviteUser) {
        log.debug("Request to update InviteUser : {}", inviteUser);
        return inviteUserRepository.save(inviteUser);
    }

    /**
     * Partially update a inviteUser.
     *
     * @param inviteUser the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<InviteUser> partialUpdate(InviteUser inviteUser) {
        log.debug("Request to partially update InviteUser : {}", inviteUser);

        return inviteUserRepository
            .findById(inviteUser.getId())
            .map(existingInviteUser -> {
                if (inviteUser.getLogin() != null) {
                    existingInviteUser.setLogin(inviteUser.getLogin());
                }
                if (inviteUser.getEmail() != null) {
                    existingInviteUser.setEmail(inviteUser.getEmail());
                }
                if (inviteUser.getStatus() != null) {
                    existingInviteUser.setStatus(inviteUser.getStatus());
                }
                if (inviteUser.getCreatedAt() != null) {
                    existingInviteUser.setCreatedAt(inviteUser.getCreatedAt());
                }
                if (inviteUser.getUpdatedAt() != null) {
                    existingInviteUser.setUpdatedAt(inviteUser.getUpdatedAt());
                }
                if (inviteUser.getCreatedId() != null) {
                    existingInviteUser.setCreatedId(inviteUser.getCreatedId());
                }
                if (inviteUser.getCreatedBy() != null) {
                    existingInviteUser.setCreatedBy(inviteUser.getCreatedBy());
                }
                if (inviteUser.getActivationKey() != null) {
                    existingInviteUser.setActivationKey(inviteUser.getActivationKey());
                }
                if (inviteUser.getActivationDate() != null) {
                    existingInviteUser.setActivationDate(inviteUser.getActivationDate());
                }
                if (inviteUser.getLangKey() != null) {
                    existingInviteUser.setLangKey(inviteUser.getLangKey());
                }
                if (inviteUser.getAuthorities() != null) {
                    existingInviteUser.setAuthorities(inviteUser.getAuthorities());
                }

                return existingInviteUser;
            })
            .map(inviteUserRepository::save);
    }

    /**
     * Get all the inviteUsers.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<InviteUser> findAll(Pageable pageable) {
        log.debug("Request to get all InviteUsers");
        return inviteUserRepository.findAll(pageable);
    }

    /**
     * Get one inviteUser by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<InviteUser> findOne(Long id) {
        log.debug("Request to get InviteUser : {}", id);
        return inviteUserRepository.findById(id);
    }

    /**
     * Delete the inviteUser by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete InviteUser : {}", id);
        inviteUserRepository.deleteById(id);
    }
}
