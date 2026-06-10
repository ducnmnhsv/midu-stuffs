package com.difisoft.marketcollector.model.realtime;

import com.difisoft.htsconnection.socket.message.receive.AdvertisedAutoItem;
import lombok.Data;

@Data
public class AdvertisedData extends TransformData<AdvertisedAutoItem> {
    private String secId;
    private String traderId;
    private String sellBuyType;
    private double price;
    private long quantity;
    private long ptVolume;
    private double ptValue;
    private boolean isCancel;
    private String contact;
    private String marketType;

    @Override
    public void parse(AdvertisedAutoItem item) {
        this.setCode(item.getStockCode().getValue());
        this.setTime(item.getTime().getValue());
        this.setSecId(item.getSecId().getValue());
        this.setTraderId(item.getTraderId().getValue());
        this.setSellBuyType(item.getSellBuyType().getValue());
        this.setPrice(item.getPrice().getValue());
        this.setQuantity(item.getQuantity().getValue());
        this.setPtVolume(item.getPutThroughVolume().getValue());
        this.setPtValue((double) item.getPutThroughValue().getValue() * 1000000);
        this.setContact(item.getContact().getValue());
    }

    @Override
    public Object toRealObject() {
        return this;
    }

}
