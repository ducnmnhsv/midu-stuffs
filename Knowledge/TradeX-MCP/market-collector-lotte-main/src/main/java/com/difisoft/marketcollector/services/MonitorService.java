package com.difisoft.marketcollector.services;

import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.services.realtime.ThreadHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Data
class Info<T> {
    private int count;
    private T first;
    private T last;
    private long firstTime;
    private long lastTime;

    public void rcv(T data) {
        if (count == 0) {
            first = data;
            firstTime = System.currentTimeMillis();
        }
        last = data;
        lastTime = System.currentTimeMillis();
        count++;
    }
}

class CodeInfo {
    int count;

    public void rcv() {
        count++;
    }

    public void reset() {
        this.count = 0;
    }
}

@Data
class Db {
    private List<String> orders;
}

@Service
public class MonitorService {
    private static final Logger log = LoggerFactory.getLogger(MonitorService.class);

    private ConcurrentHashMap<Class, Info> realtimeMsgInfo = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, CodeInfo> codeMsgInfo = new ConcurrentHashMap<>();
    private Db db = new Db();
    private LinkedBlockingQueue<ThreadHandler.Data> messageQueue = new LinkedBlockingQueue<>();

    private ObjectMapper objectMapper;
    private AppConf appConf;

    @Autowired
    public MonitorService(
            ObjectMapper objectMapper,
            AppConf appConf
    ) {
        this.objectMapper = objectMapper;
        this.appConf = appConf;
    }

    @PostConstruct
    public void init() {
        this.readDataFromFile();
        new Thread(() -> {
            while (true) {
                try {
                    ThreadHandler.Data t = messageQueue.take();
                    realtimeMsgInfo.computeIfAbsent(t.getClass(), k -> new Info()).rcv(t);
                    String code = t.getItem().getCode().getValue();
                    if (code != null) {
                        codeMsgInfo.computeIfAbsent(code, k -> new CodeInfo()).rcv();
                    }
                } catch (InterruptedException ie) {
                    // swallow
                } catch (Exception e) {
                    log.error("fail to handle monitor message", e);
                }
            }
        }).start();
    }

    @Scheduled(cron = "${app.schedulers.saveMonitorData}")
    public void scheduleSavingData() {
        this.writeDataToFile();
    }

    @Scheduled(cron = "${app.schedulers.resetMonitorData}")
    public void resetMonitorData() {
        this.codeMsgInfo.forEach((k, v) -> v.reset());
    }

    public <T extends ThreadHandler.Data> void rcvRealtimeMsg(T msg) {
        try {
            messageQueue.put(msg);
        } catch (InterruptedException ie) {
            // swallow
        }
    }

    public Map<Class, Info> queryRealtimeMsg() {
        Map<Class, Info> result = new HashMap<>();
        this.realtimeMsgInfo.forEach((k, v) -> result.put(k, v));
        return result;
    }

    public void writeDataToFile() {
        db.setOrders(codeMsgInfo.entrySet().stream().
                sorted((v1, v2) -> v2.getValue().count - v1.getValue().count).
                map(v -> v.getKey()).collect(Collectors.toList()));
        try {
            this.objectMapper.writeValue(new File(appConf.getRealtime().getMonitorDataFile()), this.db);
        } catch (Exception e) {
            log.error("fail to write data to file {}", appConf.getRealtime().getMonitorDataFile(), e);
        }
    }

    public void readDataFromFile() {
        try {
            this.db = this.objectMapper.readValue(new File(appConf.getRealtime().getMonitorDataFile()), Db.class);
        } catch (Exception e) {
            log.error("fail to read data from file {}", appConf.getRealtime().getMonitorDataFile(), e);
        }
    }

    public List<String> getOrderSymbols() {
        return this.db.getOrders();
    }
}
