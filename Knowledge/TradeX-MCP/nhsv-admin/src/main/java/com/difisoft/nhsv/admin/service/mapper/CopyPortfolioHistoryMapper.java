package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.CopyPortfolioHistory;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioHistoryDTO;
import com.difisoft.nhsv.admin.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CopyPortfolioHistory} and its DTO {@link CopyPortfolioHistoryDTO}.
 */
@Mapper(componentModel = "spring")
public interface CopyPortfolioHistoryMapper extends EntityMapper<CopyPortfolioHistoryDTO, CopyPortfolioHistory> {
    @Mapping(target = "mlUserId", source = "mlUserId", qualifiedByName = "userLogin")
    CopyPortfolioHistoryDTO toDto(CopyPortfolioHistory s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
