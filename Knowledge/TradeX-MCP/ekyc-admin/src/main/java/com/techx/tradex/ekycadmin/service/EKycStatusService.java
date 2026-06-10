package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.common.exceptions.FieldRequiredException;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.ekycadmin.domain.EKyc;
import com.techx.tradex.ekycadmin.domain.EKycCreatorStatus;
import com.techx.tradex.ekycadmin.models.request.EKycStatusReq;
import com.techx.tradex.ekycadmin.models.response.EKycStatusRes;
import com.techx.tradex.ekycadmin.repository.CustomEKycCreatorStatusRepository;
import com.techx.tradex.ekycadmin.repository.CustomEKycRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link EKyc}.
 */
@Service
@Slf4j
@Transactional
public class EKycStatusService {

    private final CustomEKycRepository eKycRepository;
    private final CustomEKycCreatorStatusRepository eKycCreatorStatusRepository;

    public EKycStatusService(CustomEKycRepository eKycRepository, CustomEKycCreatorStatusRepository eKycCreatorStatusRepository) {
        this.eKycRepository = eKycRepository;
        this.eKycCreatorStatusRepository = eKycCreatorStatusRepository;
    }

    @Transactional
    public List<EKycStatusRes> getEKycStatus(EKycStatusReq request) {
        log.debug("Request get Status : {}", request);
        if (request.getIds() == null) {
            throw new FieldRequiredException("ids");
        }
        List<Long> ids = new ArrayList<Long>();
        try {
            if (request.getIds() instanceof List) {
                for (Object id : ((List) request.getIds())) {
                    if (id instanceof String) {
                        ids.add(Long.valueOf((String) id));
                    }
                }
            } else if (request.getIds() instanceof String) {
                String[] req = ((String) request.getIds()).split(",");
                if (req.length > 0) {
                    for (String id : req) {
                        ids.add(Long.valueOf(id));
                    }
                } else {
                    ids.add(Long.valueOf((String) request.getIds()));
                }
            }
        } catch (Exception e) {
            throw new GeneralException("CAN_NOT_CONVERT_IDS");
        }

        List<EKyc> ekycs = eKycRepository.findByIdIn(ids);
        Map<Long, EKycCreatorStatus> creatorStatuses = eKycCreatorStatusRepository
            .findByIdIn(ids)
            .stream()
            .collect(Collectors.toMap(it -> it.getEKyc().getId(), Function.identity()));
        return ekycs
            .stream()
            .map(
                ekyc -> {
                    EKycStatusRes res = new EKycStatusRes();
                    res.setId(ekyc.getId());
                    res.setIdentifierId(ekyc.getIdentifierId());
                    res.setPhoneNumber(ekyc.getPhoneNo());
                    res.setFullName(ekyc.getFullName());
                    res.setStatus(ekyc.getStatus().name());
                    EKycCreatorStatus eKycCreatorStatus = creatorStatuses.get(ekyc.getId());
                    if (eKycCreatorStatus != null) {
                        res.setCreatorStatus(eKycCreatorStatus.getStatus());
                        res.setCreatorReason(eKycCreatorStatus.getReason());
                        res.setCreatorFullResult(eKycCreatorStatus.getFullResult());
                    }
                    return res;
                }
            )
            .collect(Collectors.toList());
    }
}
