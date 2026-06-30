package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLoss;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderProfitLossDTO;
import com.difisoft.nhsv.admin.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CopyMarketLeaderProfitLoss} and its DTO {@link CopyMarketLeaderProfitLossDTO}.
 */
@Mapper(componentModel = "spring")
public interface CopyMarketLeaderProfitLossMapper extends EntityMapper<CopyMarketLeaderProfitLossDTO, CopyMarketLeaderProfitLoss> {
    @Mapping(target = "mlUserId", source = "mlUserId", qualifiedByName = "userId")
    CopyMarketLeaderProfitLossDTO toDto(CopyMarketLeaderProfitLoss s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);
}
