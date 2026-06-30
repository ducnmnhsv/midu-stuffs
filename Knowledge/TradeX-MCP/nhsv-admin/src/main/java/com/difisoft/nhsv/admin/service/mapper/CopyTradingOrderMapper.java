package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.CopyTradingOrder;
import com.difisoft.nhsv.admin.service.dto.CopyTradingOrderDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link CopyTradingOrder} and its DTO {@link CopyTradingOrderDTO}.
 */
@Mapper(componentModel = "spring")
public interface CopyTradingOrderMapper extends EntityMapper<CopyTradingOrderDTO, CopyTradingOrder> {}
