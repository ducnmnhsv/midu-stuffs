package com.difisoft.marketcollector.model.realtime;

import com.difisoft.htsconnection.socket.message.receive.A78AutoItem;
import com.difisoft.marketcollector.constants.MarketTypeEnum;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

@Data
public class MarketStatusData extends TransformData<A78AutoItem> {
    private static final Logger log = LoggerFactory.getLogger(MarketStatusData.class);
    private int id;
    private String title;
    private String market;
    private String status;
    private String type = "EQUITY";


    @Override
    public void parse(A78AutoItem item) {
        this.setId(item.getId().getValue());
        this.setCode(item.getCode().getValue());
        this.setTitle(item.getTitle().getValue());
        this.setTime(item.getTime().getValue() + "");
    }

    @Override
    public void formatTime() {
    }

    @Override
    public void parseStatus(Map<String, String> statusMap) {
        this.setType("EQUITY");
        String market;
        if (this.getTitle().contains(MarketTypeEnum.HNX.name())) {
            market = MarketTypeEnum.HNX.name();
        } else if (this.getTitle().contains(MarketTypeEnum.HOSE.name())) {
            market = MarketTypeEnum.HOSE.name();
        } else if (this.getTitle().contains(MarketTypeEnum.UPCOM.name())) {
            market = MarketTypeEnum.UPCOM.name();
        } else {
            log.error("Ignore A78 cause of market not found");
            throw new IgnoreException("Ignore A78 cause of market not found:" + this.getTitle());
        }
        String status = statusMap.get(this.getTitle());
        if (status == null) {
            log.error("Ignore cause of status not found");
            throw new IgnoreException("Ignore cause of status not found");
        }
        this.setStatus(status);
        this.setMarket(market);
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String timeStr = leftPad(this.getTime(), 6, '0');
        try {
            Date date = new SimpleDateFormat("yyyyMMddHHmmss").parse(dateStr + timeStr);
            Date utcDate = new Date(date.getTime() - 7 * 3600000);
            String utcTime = new SimpleDateFormat("HHmmss").format(utcDate);
            this.setTime(utcTime);
        } catch (ParseException e) {
            log.error("Error parsing time {0}", e);
        }
    }

    public static String leftPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        if (pads > 8192) { //PAD_LIMIT
            return leftPad(str, size, padChar);
        }
        return repeat(padChar, pads).concat(str);
    }

    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return "";
        }
        final char[] buf = new char[repeat];
        Arrays.fill(buf, ch);
        return new String(buf);
    }
}
