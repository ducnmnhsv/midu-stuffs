package com.difisoft.marketcollector.model.realtime;

import lombok.Data;

@Data
public class ExtraUpdate {
    private String code;
    private Double basis;

    private long ptVolume;
    private double ptValue;

    public static ExtraUpdate fromDealNotice(DealNoticeData dealNoticeData) {
        ExtraUpdate extraUpdate = new ExtraUpdate();
        extraUpdate.setCode(dealNoticeData.getCode());
        extraUpdate.setPtValue(dealNoticeData.getPtValue());
        extraUpdate.setPtVolume(dealNoticeData.getPtVolume());
        return extraUpdate;
    }
}
