package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.CopySubscriber;
import com.difisoft.nhsv.admin.domain.CopySubscriberDetails;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberDTO;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberDetailsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CopySubscriberDetails} and its DTO {@link CopySubscriberDetailsDTO}.
 */
@Mapper(componentModel = "spring")
public interface CopySubscriberDetailsMapper extends EntityMapper<CopySubscriberDetailsDTO, CopySubscriberDetails> {
    @Mapping(target = "copySubscriber", source = "copySubscriber", qualifiedByName = "copySubscriberId")
    CopySubscriberDetailsDTO toDto(CopySubscriberDetails s);

    @Named("copySubscriberId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CopySubscriberDTO toDtoCopySubscriberId(CopySubscriber copySubscriber);
}
