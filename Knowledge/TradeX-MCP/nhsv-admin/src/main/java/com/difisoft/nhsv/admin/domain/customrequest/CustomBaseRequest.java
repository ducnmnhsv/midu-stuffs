package com.difisoft.nhsv.admin.domain.customrequest;

import java.util.Objects;

public interface CustomBaseRequest {
    default int buildDefaultPageNumber(Integer pageNumber) {
        return Objects.isNull(pageNumber) || pageNumber < 0 ? 0 : pageNumber;
    }

    default int buildDefaultPageSize(Integer pageSize) {
        return Objects.isNull(pageSize) || pageSize < 0 ? 20 : pageSize;
    }

    default boolean buildDefaultSortAsc(Boolean sortAsc) {
        return Objects.isNull(sortAsc) ? Boolean.TRUE : sortAsc;
    }
}
