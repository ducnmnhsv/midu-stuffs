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
public class EventTypeID2Dto implements IVietStockEventDto {
    @JsonProperty("EventID")
    protected Long eventId;

    @JsonProperty("Code")
    protected String code;

    @JsonProperty("ChannelID")
    private Integer channelId;

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

    @JsonProperty("Note")
    private String note;

    @JsonProperty("EventTypeID")
    private Integer eventTypeId;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Exchange")
    private String exchange;

    @JsonProperty("Source")
    private String source;

    @JsonProperty("Title")
    private String title;

    @JsonProperty("Content")
    private String content;

    @JsonProperty("FileUrl")
    private String fileUrl;

    @JsonProperty("DateOrder")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long dateOrder;

    @JsonProperty("Volume")
    private Long volume;

    @JsonProperty("Price")
    private Double price;

    @JsonProperty("Reason")
    private String reason;

    @JsonProperty("ReasonCBKS")
    private String reasonCBKS;

    @JsonProperty("DateNYTL")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long dateNYTL;

    @JsonProperty("DateHCB")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long dateHCB;

    @JsonProperty("DateGDTL")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long dateGDTL;

    @JsonProperty("DateCTGD")
    @JsonDeserialize(using = CustomVietStockDateDeserializer.class)
    private Long dateCTGD;

    @JsonProperty("CancelVolume")
    private Long cancelVolume;

    @JsonProperty("TimeDPGD")
    private String timeDPGD;

    @JsonProperty("Row")
    private Integer row;
}
