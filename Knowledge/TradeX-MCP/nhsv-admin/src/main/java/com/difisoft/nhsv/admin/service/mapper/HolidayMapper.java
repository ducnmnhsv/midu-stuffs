package com.difisoft.nhsv.admin.service.mapper;

import com.difisoft.nhsv.admin.domain.Holiday;
import com.difisoft.nhsv.admin.service.dto.HolidayDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Holiday} and its DTO {@link HolidayDTO}.
 */
@Mapper(componentModel = "spring")
public interface HolidayMapper extends EntityMapper<HolidayDTO, Holiday> {}
