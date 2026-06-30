package com.difisoft.nhsv.admin.domain.response;

import com.difisoft.nhsv.admin.constant.Constants;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Configuration
public class GenericResponse<T> {

    @JsonProperty
    private LocalDateTime timestamp;
    @JsonProperty
    private int status;
    @JsonProperty
    private String message;
    @JsonProperty
    private T data;
    @JsonProperty
    private PageData pageData;

    public static <E> GenericResponse<E> internalServerError(String message) {
        return GenericResponse.<E>builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message(StringUtils.isBlank(message) ? HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() : MessageFormat.format(Constants.MTS_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), message))
            .build();
    }

    public static <E> GenericResponse<E> success(String message) {
        return GenericResponse.<E>builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.OK.value())
            .message(StringUtils.isBlank(message) ? HttpStatus.OK.getReasonPhrase() : MessageFormat.format(Constants.MTS_MESSAGE, HttpStatus.OK.getReasonPhrase(), message))
            .build();
    }

    public static <E> GenericResponse<E> badRequest(String message) {
        return GenericResponse.<E>builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .message(StringUtils.isBlank(message) ? HttpStatus.BAD_REQUEST.getReasonPhrase() : MessageFormat.format(Constants.MTS_MESSAGE, HttpStatus.BAD_REQUEST.getReasonPhrase(), message))
            .build();
    }

    public static void customBuildingPageData(GenericResponse response, List items, int pageNumber, int pageSize) {
        int totalElements = CollectionUtils.isEmpty(items) ? 0 : items.size();

        response.setPageData(GenericResponse.PageData.builder()
            .pageSize(pageSize)
            .pageNumber(pageNumber)
            .totalPages(totalElements / pageSize + (totalElements % pageSize > 0 ? 1 : 0))
            .totalElements(totalElements)
            .build());

        int fromIndex = Math.min(pageNumber * pageSize, totalElements);
        int toIndex = Math.min(fromIndex + pageSize, totalElements);

        List data = new ArrayList<>();
        data.addAll(items.subList(fromIndex, toIndex));

        response.setData(data);
    }

    public static void buildingPageData(GenericResponse response, Page page) {
        response.setPageData(
            GenericResponse.PageData.builder()
                .pageSize(page.getSize()).pageNumber(page.getNumber())
                .totalPages(page.getTotalPages()).totalElements(page.getTotalElements())
                .build()
        );
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Configuration
    public static class PageData {
        @JsonProperty
        private int pageSize;
        @JsonProperty
        private int pageNumber;
        @JsonProperty
        private long totalElements;
        @JsonProperty
        private int totalPages;
    }
}
