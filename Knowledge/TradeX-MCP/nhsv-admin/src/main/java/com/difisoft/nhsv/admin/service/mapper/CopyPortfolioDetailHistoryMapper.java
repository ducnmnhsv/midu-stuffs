package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.CopyPortfolioDetailHistory;
import com.difisoft.nhsv.admin.domain.CopyPortfolioHistory;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioDetailHistoryDTO;
import com.difisoft.nhsv.admin.service.dto.CopyPortfolioHistoryDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CopyPortfolioDetailHistory} and its DTO {@link CopyPortfolioDetailHistoryDTO}.
 */
@Mapper(componentModel = "spring")
public interface CopyPortfolioDetailHistoryMapper extends EntityMapper<CopyPortfolioDetailHistoryDTO, CopyPortfolioDetailHistory> {
    @Mapping(target = "copyPortfolioHistoryId", source = "copyPortfolioHistoryId", qualifiedByName = "copyPortfolioHistoryId")
    CopyPortfolioDetailHistoryDTO toDto(CopyPortfolioDetailHistory s);

    @Named("copyPortfolioHistoryId")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    CopyPortfolioHistoryDTO toDtoCopyPortfolioHistoryId(CopyPortfolioHistory copyPortfolioHistory);
}
