package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderDetails;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderDetailsDTO;
import com.difisoft.nhsv.admin.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CopyMarketLeaderDetails} and its DTO {@link CopyMarketLeaderDetailsDTO}.
 */
@Mapper(componentModel = "spring")
public interface CopyMarketLeaderDetailsMapper extends EntityMapper<CopyMarketLeaderDetailsDTO, CopyMarketLeaderDetails> {
    @Mapping(target = "mlUserId", source = "mlUserId", qualifiedByName = "userLogin")
    CopyMarketLeaderDetailsDTO toDto(CopyMarketLeaderDetails s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
