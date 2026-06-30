package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.CopySubscriberHistory;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.service.dto.CopySubscriberHistoryDTO;
import com.difisoft.nhsv.admin.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CopySubscriberHistory} and its DTO {@link CopySubscriberHistoryDTO}.
 */
@Mapper(componentModel = "spring")
public interface CopySubscriberHistoryMapper extends EntityMapper<CopySubscriberHistoryDTO, CopySubscriberHistory> {
    @Mapping(target = "mlUserId", source = "mlUserId", qualifiedByName = "userLogin")
    CopySubscriberHistoryDTO toDto(CopySubscriberHistory s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
