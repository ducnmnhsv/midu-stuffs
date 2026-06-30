package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.CopyPortfolio;
import com.difisoft.nhsv.admin.domain.CopyPortfolioDetails;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDTO;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailsDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CopyPortfolioDetails} and its DTO {@link CopyPortfolioDetailsDTO}.
 */
@Mapper(componentModel = "spring")
public interface CopyPortfolioDetailsMapper extends EntityMapper<CopyPortfolioDetailsDTO, CopyPortfolioDetails> {
    @Mapping(target = "copyPortfolioId", source = "copyPortfolioId", qualifiedByName = "copyPortfolioId")
    CopyPortfolioDetailsDTO toDto(CopyPortfolioDetails s);

    @Named("copyPortfolioId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CopyPortfolioDTO toDtoCopyPortfolioId(CopyPortfolio copyPortfolio);
}
