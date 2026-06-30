package com.difisoft.nhsv.admin.service;

import com.difisoft.nhsv.admin.domain.*; // for static metamodels
import com.difisoft.nhsv.admin.domain.ChatRoom;
import com.difisoft.nhsv.admin.repository.ChatRoomRepository;
import com.difisoft.nhsv.admin.service.criteria.ChatRoomPrimaryCriteria;

import java.util.List;
import javax.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

@Service
@Transactional(readOnly = true)
@Primary
public class ChatRoomPrimaryQueryService extends QueryService<ChatRoom> {

    private final Logger log = LoggerFactory.getLogger(ChatRoomQueryService.class);

    private final ChatRoomRepository chatRoomRepository;

    public ChatRoomPrimaryQueryService(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> findByCriteria(ChatRoomPrimaryCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<ChatRoom> specification = createSpecification(criteria);
        return chatRoomRepository.findAll(specification);
    }

    @Transactional(readOnly = true)
    public Page<ChatRoom> findByCriteria(ChatRoomPrimaryCriteria criteria, Pageable page) {
        log.info("find by criteria : {}, page: {}", criteria, page);
        final Specification<ChatRoom> specification = createSpecification(criteria);
        return chatRoomRepository.findAll(specification, page);
    }

    protected Specification<ChatRoom> createSpecification(ChatRoomPrimaryCriteria criteria) {
        Specification<ChatRoom> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getDistinct() != null) {
                specification = specification.and(distinct(criteria.getDistinct()));
            }
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), ChatRoom_.id));
            }
            if (criteria.getGroupName() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getGroupName(), ChatRoom_.groupName));
            }
            if (criteria.getGroupOwner() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getGroupOwner(), ChatRoom_.groupOwner));
            }
            if (criteria.getIntroduction() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getIntroduction(), ChatRoom_.introduction));
            }
            if (criteria.getPhoto() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPhoto(), ChatRoom_.photo));
            }
            if (criteria.getBrokerName() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getBrokerName(), ChatRoom_.brokerName));
            }
            if (criteria.getBrokerContact() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getBrokerContact(), ChatRoom_.brokerContact));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), ChatRoom_.status));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getCreatedBy(), ChatRoom_.createdBy));
            }
            if (criteria.getCreatedAt() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getCreatedAt(), ChatRoom_.createdAt));
            }
            if (criteria.getUpdatedAt() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getUpdatedAt(), ChatRoom_.updatedAt));
            }
            if (criteria.getApprovedAt() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getApprovedAt(), ChatRoom_.approvedAt));
            }
            if (criteria.getRejectedAt() != null) {
                specification = specification
                        .and(buildRangeSpecification(criteria.getRejectedAt(), ChatRoom_.rejectedAt));
            }
            if (criteria.getRejectReason() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getRejectReason(), ChatRoom_.rejectReason));
            }
            if (criteria.getApprovedBy() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getApprovedBy(), ChatRoom_.approvedBy));
            }
            if (criteria.getRejectedBy() != null) {
                specification = specification
                        .and(buildStringSpecification(criteria.getRejectedBy(), ChatRoom_.rejectedBy));
            }
            if (criteria.getAction() != null) {
                specification = specification.and(buildSpecification(criteria.getAction(), ChatRoom_.action));
            }
            if (criteria.getSocialLinkId() != null) {
                specification = specification.and(
                        buildSpecification(
                                criteria.getSocialLinkId(),
                                root -> root.join(ChatRoom_.socialLinks, JoinType.LEFT).get(SocialLink_.id)));
            }
            if (criteria.getSearch() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSearch(), ChatRoom_.groupName)
                        .or(buildStringSpecification(criteria.getSearch(), ChatRoom_.groupOwner)));
            }
        }
        return specification;
    }
}
