package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.CopyTradingRegister;
import com.difisoft.nhsv.admin.service.dto.CopyTradingRegisterDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity {@link CopyTradingRegister} and its DTO {@link CopyTradingRegisterDTO}.
 */
@Mapper(componentModel = "spring")
public interface CopyTradingRegisterMapper extends EntityMapper<CopyTradingRegisterDTO, CopyTradingRegister> {
}
