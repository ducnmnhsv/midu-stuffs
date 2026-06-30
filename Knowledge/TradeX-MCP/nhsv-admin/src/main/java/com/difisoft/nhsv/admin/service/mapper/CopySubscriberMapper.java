package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.CopySubscriber;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberDTO;
import com.difisoft.nhsv.admin.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CopySubscriber} and its DTO {@link CopySubscriberDTO}.
 */
@Mapper(componentModel = "spring")
public interface CopySubscriberMapper extends EntityMapper<CopySubscriberDTO, CopySubscriber> {
    @Mapping(target = "mlUserId", source = "mlUserId", qualifiedByName = "userLogin")
    CopySubscriberDTO toDto(CopySubscriber s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
