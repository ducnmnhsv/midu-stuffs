package com.difisoft.marketcollector.model.realtime;

import com.difisoft.htsconnection.socket.message.receive.TickerMesgItem;
import lombok.Data;

@Data
public class TickerMesgData extends TransformData<TickerMesgItem> {
    private String click;
    private String color;
    private String filter;
    private int sendDate;
    private int sendTime;
    private String sendId;
    private String receiveId;
    private String title;
    private String content;

    @Override
    public void parse(TickerMesgItem item) {
        this.setClick(item.getClick().getValue());
        this.setColor(item.getColor().getValue());
        this.setFilter(item.getFilter().getValue());
        this.setSendDate(item.getSendDate().getValue());
        this.setSendTime(item.getSendTime().getValue());
        this.setSendId(item.getSendId().getValue());
        this.setReceiveId(item.getReceiveId().getValue());
        this.setTitle(item.getTitle().getValue());
        this.setContent(item.getContent().getValue());
    }
}
