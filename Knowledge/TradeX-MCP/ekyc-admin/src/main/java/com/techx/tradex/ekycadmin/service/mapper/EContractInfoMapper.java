package com.techx.tradex.ekycadmin.service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.techx.tradex.ekycadmin.domain.*;
import com.techx.tradex.ekycadmin.service.dto.EContractInfoDTO;

/**
 * Mapper for the entity {@link EContractInfo} and its DTO {@link EContractInfoDTO}.
 */
@Mapper(componentModel = "spring", uses = { EContractMapper.class })
public interface EContractInfoMapper extends EntityMapper<EContractInfoDTO, EContractInfo> {
    @Mapping(target = "id", source = "id")
    EContractInfoDTO toDto(EContractInfo s);
}
