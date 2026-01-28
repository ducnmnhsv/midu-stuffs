package com.difisoft.marketcollector.model.realtime;

import com.difisoft.htsconnection.socket.message.receive.DealNoticeAutoItem;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

@Data
public class DealNoticeData extends TransformData<DealNoticeAutoItem> {
    private static final Logger log = LoggerFactory.getLogger(DealNoticeData.class);

    private String confirmNumber;
    private String marketType;
    private long matchPrice;
    private long matchVolume;
    private long ptVolume;
    private double ptValue;
    private boolean isCancel;
    private double matchValue;

    @Override
    public void parse(DealNoticeAutoItem item) {
        this.setCode(item.getStockCode().getValue());
        this.setTime(item.getTime().getValue());
        this.setConfirmNumber(String.valueOf(item.getConfirmNo().getValue()));
        this.setMatchPrice(item.getPutThroughMatchPrice().getValue());
        this.setMatchVolume(item.getPutThroughMatchVolume().getValue());
        this.setMatchValue((double) this.getMatchPrice() * this.getMatchVolume());
        this.setPtVolume(item.getPutThroughVolume().getValue());
        this.setPtValue((double) item.getPutThrough().getValue() * 1000000);
        this.setCancel(false);
    }

    public void formatTime() {
        Date date = new Date();
        this.time = sdfTime.format(date);
        this.date = sdf.format(date);
    }

    @Override
    public Object toRealObject() {
        return this;
    }

}
