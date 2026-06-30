package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.CopyPortfolio;
import com.difisoft.nhsv.admin.domain.User;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDTO;
import com.difisoft.nhsv.admin.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CopyPortfolio} and its DTO {@link CopyPortfolioDTO}.
 */
@Mapper(componentModel = "spring")
public interface CopyPortfolioMapper extends EntityMapper<CopyPortfolioDTO, CopyPortfolio> {
    @Mapping(target = "mlUserId", source = "mlUserId", qualifiedByName = "userLogin")
    CopyPortfolioDTO toDto(CopyPortfolio s);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
