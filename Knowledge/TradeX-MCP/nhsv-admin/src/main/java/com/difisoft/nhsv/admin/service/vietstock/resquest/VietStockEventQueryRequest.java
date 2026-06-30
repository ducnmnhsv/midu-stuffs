package com.difisoft.nhsv.admin.service.vietstock.resquest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VietStockEventQueryRequest {

    /**
     * Event type identifier
     */
    private String eventTypeID;

    /**
     * Channel identifier
     */
    private String channelID;

    /**
     * Category identifier
     */
    private String catID;

    /**
     * Stock symbol code
     */
    @Builder.Default
    private String code = "";

    /**
     * From date in yyyy-MM-dd format
     */
    private String fDate;

    /**
     * To date in yyyy-MM-dd format
     */
    private String tDate;

    /**
     * Page number for pagination
     */
    @Builder.Default
    private Integer page = 1;

    /**
     * Number of items per page
     */
    @Builder.Default
    private Integer pageSize = 50;

    /**
     * Field to order results by
     */
    @Builder.Default
    private String orderBy = "Date1";

    /**
     * Sort direction (ASC/DESC)
     */
    @Builder.Default
    private String orderDir = "DESC";

    /**
     * Authentication cookies
     */
    private String cookies;

    /**
     * CSRF verification token
     */
    private String requestToken;

    public String toFormEncodedString() {
        return "eventTypeID=" + eventTypeID +
            "&channelID=" + channelID +
            "&code=" + code +
            "&catID=" + catID +
            "&fDate=" + fDate +
            "&tDate=" + tDate +
            "&page=" + page +
            "&pageSize=" + pageSize +
            "&orderBy=" + orderBy +
            "&orderDir=" + orderDir +
            "&__RequestVerificationToken=" + requestToken;
    }
}
