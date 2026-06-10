package com.techx.tradex.ekycadmin.service.mapper;

import com.techx.tradex.ekycadmin.domain.EContract;
import com.techx.tradex.ekycadmin.service.dto.EContractDTO;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link EContract} and its DTO {@link EContractDTO}.
 */
@Mapper(componentModel = "spring")
public interface EContractMapper extends EntityMapper<EContractDTO, EContract> {
    @Mapping(target = "id", source = "id")
    EContractDTO toDto(EContract s);
}
