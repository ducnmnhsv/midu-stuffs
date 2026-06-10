package com.difisoft.marketcollector.model.realtime;

import com.difisoft.htsconnection.socket.message.receive.IndexAutoItem;
import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.marketcollector.services.CacheService;
import com.difisoft.model.utils.NumberUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IndexUpdateData extends TransformData<IndexAutoItem> {
    private double open;
    private double high;
    private double low;
    private double last;
    private double change;
    private double rate;
    private long tradingVolume;
    private long tradingValue;
    private long matchingVolume;
    private int upCount;
    private int ceilingCount;
    private int unchangedCount;
    private int downCount;
    private int floorCount;
    private String type = SymbolTypeEnum.INDEX.name();

    @JsonIgnore
    private List<Session> sessions;

    @Data
    public static class Session {
        private double last;
        private double change;
        private double rate;
        private long tradingVolume;
        private long tradingValue;

        public boolean hasValue() {
            return this.last != 0
                    && this.change != 0
                    && this.rate != 0
                    && this.tradingValue != 0
                    && this.tradingVolume != 0;
        }
    }


    @Override
    public void validate() {
        if (this.open == 0 || this.high == 0 || this.low == 0 || this.last == 0) {
            throw new IgnoreException("ignore because one of condition is true 'this.open == 0 || this.high == 0 || this.low == 0 || this.last == 0'");
        }
    }

    @Override
    public void parse(IndexAutoItem idx) {
        this.setCode(idx.getIndexCode().getValue());
        this.setTime(idx.getTime().getValue());
        this.setOpen(NumberUtils.round2DecimalFloatToDouble(idx.getOpen().getValue()));
        this.setHigh(NumberUtils.round2DecimalFloatToDouble(idx.getHigh().getValue()));
        this.setLow(NumberUtils.round2DecimalFloatToDouble(idx.getLow().getValue()));
        this.setChange(NumberUtils.round2DecimalFloatToDouble(idx.getChange().getValue()));
        this.setLast(NumberUtils.round2DecimalFloatToDouble(idx.getLast().getValue()));
        this.setRate(NumberUtils.round2DecimalFloatToDouble(idx.getRate().getValue()));
        this.setTradingVolume(idx.getVolume().getValue());
        this.setTradingValue(idx.getValue().getValue() * 1000000);
        this.setMatchingVolume(idx.getMatchVolume().getValue());
        this.setCeilingCount(idx.getCeilingCount().getValue());
        this.setUpCount(idx.getUpCount().getValue());
        this.setUnchangedCount(idx.getSameCount().getValue());
        this.setDownCount(idx.getDownCount().getValue());
        this.setFloorCount(idx.getFloorCount().getValue());
        this.setSessions(new ArrayList<>());
        Session session = new Session();
        session.setLast(idx.getSession1Last().getValue());
        session.setChange(idx.getSession1Change().getValue());
        session.setRate(idx.getSession1Rate().getValue());
        session.setTradingVolume(idx.getSession1Volume().getValue());
        session.setTradingValue(idx.getSession1Value().getValue());
        this.sessions.add(session);
        session = new Session();
        session.setLast(idx.getSession2Last().getValue());
        session.setChange(idx.getSession2Change().getValue());
        session.setRate(idx.getSession2Rate().getValue());
        session.setTradingVolume(idx.getSession2Volume().getValue());
        session.setTradingValue(idx.getSession2Value().getValue());
        this.sessions.add(session);
        session = new Session();
        session.setLast(NumberUtils.round2DecimalFloatToDouble(idx.getSession3Last().getValue()));
        session.setChange(NumberUtils.round2DecimalFloatToDouble(idx.getSession3Change().getValue()));
        session.setRate(NumberUtils.round2DecimalFloatToDouble(idx.getSession3Rate().getValue()));
        session.setTradingValue(idx.getSession3Volume().getValue());
        session.setTradingValue(idx.getSession3Value().getValue());
        if (session.hasValue()) {
            this.sessions.add(session);
        }

        // on KIS, sometime we got wrong HNX indexQuote from bos (example high - 103.46 but we got 10346)
        if (this.getOpen() >= 2000) {
            this.setOpen(this.getOpen() / 100);
        }
        if (this.getHigh() >= 2000) {
            this.setHigh(this.getHigh() / 100);
        }
        if (this.getLow() >= 2000) {
            this.setLow(this.getLow() / 100);
        }
        if (this.getLast() >= 2000) {
            this.setLast(this.getLast() / 100);
        }
    }

    @Override
    public void formatRefCode(CacheService cacheService) {
        this.code = cacheService.getRefIndexCodeMap().get(this.code);
        if (this.code == null) {
            throw new IgnoreException("ignore because code is null after get from ref code map");
        }
    }

    @Override
    public Object toRealObject() {
        return this;
    }
}
