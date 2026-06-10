package com.techx.tradex.realtime.model.request;

import lombok.Data;

@Data
public class SaveRedisToDatabaseRequest {
    boolean enableSaveQuote = true;
    boolean enableSaveQuoteMinute = true;
    boolean enableSaveBidAsk = true;
}
