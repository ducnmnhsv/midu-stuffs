package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.SocialLink;
import com.difisoft.nhsv.admin.repository.SocialLinkRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link SocialLink}.
 */
@Service
@Transactional
public class SocialLinkService {

    private final Logger log = LoggerFactory.getLogger(SocialLinkService.class);

    private final SocialLinkRepository socialLinkRepository;

    public SocialLinkService(SocialLinkRepository socialLinkRepository) {
        this.socialLinkRepository = socialLinkRepository;
    }

    /**
     * Save a socialLink.
     *
     * @param socialLink the entity to save.
     * @return the persisted entity.
     */
    public SocialLink save(SocialLink socialLink) {
        log.debug("Request to save SocialLink : {}", socialLink);
        return socialLinkRepository.save(socialLink);
    }

    /**
     * Update a socialLink.
     *
     * @param socialLink the entity to save.
     * @return the persisted entity.
     */
    public SocialLink update(SocialLink socialLink) {
        log.debug("Request to update SocialLink : {}", socialLink);
        return socialLinkRepository.save(socialLink);
    }

    /**
     * Partially update a socialLink.
     *
     * @param socialLink the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<SocialLink> partialUpdate(SocialLink socialLink) {
        log.debug("Request to partially update SocialLink : {}", socialLink);

        return socialLinkRepository
            .findById(socialLink.getId())
            .map(existingSocialLink -> {
                if (socialLink.getType() != null) {
                    existingSocialLink.setType(socialLink.getType());
                }
                if (socialLink.getLink() != null) {
                    existingSocialLink.setLink(socialLink.getLink());
                }

                return existingSocialLink;
            })
            .map(socialLinkRepository::save);
    }

    /**
     * Get all the socialLinks.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<SocialLink> findAll() {
        log.debug("Request to get all SocialLinks");
        return socialLinkRepository.findAll();
    }

    /**
     * Get one socialLink by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<SocialLink> findOne(Long id) {
        log.debug("Request to get SocialLink : {}", id);
        return socialLinkRepository.findById(id);
    }

    /**
     * Delete the socialLink by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete SocialLink : {}", id);
        socialLinkRepository.deleteById(id);
    }
}
