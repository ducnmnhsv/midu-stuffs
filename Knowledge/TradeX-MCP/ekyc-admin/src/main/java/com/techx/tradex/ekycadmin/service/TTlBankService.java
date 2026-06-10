package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.common.utils.Pair;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.models.ttl.ListBankBranchRequest;
import com.techx.tradex.ekycadmin.models.ttl.ListBankBranchResponse;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TTlBankService extends TimerTask {

    private static final Logger log = LoggerFactory.getLogger(TTlBankService.class);

    private final TTLApiService ttlApiService;

    private ListBankBranchResponse cache;
    private final AtomicBoolean isInit = new AtomicBoolean(false);
    private final AppConf appConf;

    @PostConstruct
    public void setup() {
        if (appConf.getCore() == null || appConf.getCore().equals("ttl")) {
            Timer time = new Timer();
            time.schedule(this, 0, 300000); // 5 minutes
        }
    }

    public TTlBankService(TTLApiService ttlApiService, AppConf appConf) {
        this.ttlApiService = ttlApiService;
        this.appConf = appConf;
    }

    public void init() {
        if (this.isInit.get()) {
            return;
        }
        this.isInit.set(true);
        while (true) {
            try {
                Pair<ListBankBranchResponse, String> pair = ttlApiService.listBankBranch(new ListBankBranchRequest());
                if ("OLS0000".equals(pair.getLeft().getErrorCode())) {
                    this.cache = pair.getLeft();
                    break;
                }
            } catch (IOException | InterruptedException e) {
                log.error("fail to query list bank", e);
            }
        }
        this.isInit.set(false);
    }

    public ListBankBranchResponse.Item findBranch(String bankId, String branchName) {
        if (this.cache == null) {
            return null;
        }
        return this.cache.getMainResult()
            .stream()
            .filter(it -> it.getBankID().equalsIgnoreCase(bankId) && it.getBankBranchDesc().equalsIgnoreCase(branchName))
            .findFirst()
            .orElse(null);
    }

    public ListBankBranchResponse.Item findBranch(String branchName) {
        if (this.cache == null) {
            return null;
        }
        return this.cache.getMainResult()
            .stream()
            .filter(it -> it.getBankBranchDesc().equalsIgnoreCase(branchName))
            .findFirst()
            .orElse(null);
    }

    @Override
    public void run() {
        this.init();
    }
}
