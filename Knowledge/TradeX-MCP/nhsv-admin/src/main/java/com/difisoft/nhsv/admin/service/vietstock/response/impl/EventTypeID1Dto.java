package com.difisoft.nhsv.admin.service.vietstock.response.impl;

import com.difisoft.nhsv.admin.service.vietstock.response.CustomVietStockDateDeserializer;
import com.difisoft.nhsv.admin.service.vietstock.response.IVietStockEventDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventTypeID1Dto implements IVietStockEventDto {
    @JsonProperty("EventID")
    protected Long eventId;

    @JsonProperty("Code")
    protected String code;

    @JsonProperty("CompanyName")
    private String companyName;

    @JsonProperty("CatID")
    private Integer catId;

    @JsonProperty("GDKHQDate")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long gdkhqDate;

    @JsonProperty("NDKCCDate")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long ndkcDate;

    @JsonProperty("Time")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long time;

    @JsonProperty("Exchange")
    private String exchange;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("FileUrl")
    private String fileUrl;

    @JsonProperty("DateOrder")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long dateOrder;

    @JsonProperty("RateTypeID")
    private Integer rateTypeId;

    @JsonProperty("Rate")
    private String rate;

    @JsonProperty("VolumePublishing")
    private Long volumePublishing;

    @JsonProperty("Row")
    private Integer row;

    // Trading event specific fields
    @JsonProperty("FormTypeID")
    private Integer formTypeId;

    @JsonProperty("Price")
    private Double price;

    @JsonProperty("ExchangeDateFrom")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long exchangeDateFrom;

    @JsonProperty("ExchangeDateTo")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long exchangeDateTo;

    @JsonProperty("ReservedDateFrom")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long reservedDateFrom;

    @JsonProperty("ReservedDateTo")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long reservedDateTo;

    // Informative event specific fields
    @JsonProperty("Content")
    private String content;

    @JsonProperty("Note")
    private String note;
}
