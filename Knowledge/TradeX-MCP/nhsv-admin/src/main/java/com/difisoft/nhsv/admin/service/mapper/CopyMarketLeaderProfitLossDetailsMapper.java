package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLoss;
import com.difisoft.nhsv.admin.domain.CopyMarketLeaderProfitLossDetails;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderProfitLossDTO;
import com.difisoft.nhsv.admin.service.dto.CopyMarketLeaderProfitLossDetailsDTO;
import com.difisoft.nhsv.admin.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CopyMarketLeaderProfitLossDetails} and its DTO {@link CopyMarketLeaderProfitLossDetailsDTO}.
 */
@Mapper(componentModel = "spring")
public interface CopyMarketLeaderProfitLossDetailsMapper
    extends EntityMapper<CopyMarketLeaderProfitLossDetailsDTO, CopyMarketLeaderProfitLossDetails> {
    @Mapping(target = "mlUserId", source = "mlUserId", qualifiedByName = "userId")
    @Mapping(
        target = "copyMarketLeaderProfitLossId",
        source = "copyMarketLeaderProfitLossId",
        qualifiedByName = "copyMarketLeaderProfitLossId"
    )
    CopyMarketLeaderProfitLossDetailsDTO toDto(CopyMarketLeaderProfitLossDetails s);

    @Named("userId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    UserDTO toDtoUserId(User user);

    @Named("copyMarketLeaderProfitLossId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CopyMarketLeaderProfitLossDTO toDtoCopyMarketLeaderProfitLossId(CopyMarketLeaderProfitLoss copyMarketLeaderProfitLoss);
}
