package com.difisoft.marketcollector.model.realtime;

import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.services.CacheService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


public abstract class TransformData<S> {
    private static final Logger log = LoggerFactory.getLogger(TransformData.class);

    private static Integer fromHour = null;
    private static Integer toHour = null;
    private static Integer hourOffset = null;
    protected static final DecimalFormat format = new DecimalFormat("##.00");
    protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    protected static final SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");

    public static void setAppConf(AppConf appConf) {
        fromHour = appConf.getRealtime().getMinHour();
        toHour = appConf.getRealtime().getMaxHour();
        hourOffset = appConf.getServerHourOffset();
    }

    @Getter
    @Setter
    protected String code;
    @Getter
    @Setter
    protected String time;
    @Getter
    @Setter
    protected String date;

    @JsonIgnore
    @Getter
    @Setter
    protected String sendOut;

    public void formatTime() {
        StringBuilder sb = new StringBuilder(6);
        String[] temps = this.time.split(":");
        int hour = Integer.valueOf(temps[0]);
        if (hour < fromHour || hour > toHour) {
            log.error("time {} not in range: {} - {}", hour, fromHour, toHour);
            throw new IgnoreException("Ignore since time is not in range");
        }
        hour = hour - hourOffset;
        if (hour < 10) {
            sb.append("0");
        }
        sb.append(hour);
        if (temps[1].length() < 2) {
            sb.append("0");
        }
        sb.append(temps[1]);
        if (temps[2].length() < 2) {
            sb.append("0");
        }
        sb.append(temps[2]);
        this.time = sb.toString();
        this.date = sdf.format(new Date());
    }

    public void validate() {
    }

    public void formatRefCode(CacheService cacheService) {
    }

    public void setExpectedChange(Double number) {
    }

    public void setExpectedRate(Double number) {
    }

    public static class IgnoreException extends RuntimeException {
        public IgnoreException(String message) {
            super(message);
        }
    }

    public void parseStatus(Map<String, String> statusMap) {
    }

    public Object toRealObject() {
        return null;
    }

    public abstract void parse(S var1);

    public TransformData<S> from(S s) {
        this.parse(s);
        return this;
    }
}
