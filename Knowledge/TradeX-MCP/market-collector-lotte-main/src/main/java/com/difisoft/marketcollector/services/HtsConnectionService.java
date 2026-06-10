package com.difisoft.marketcollector.services;

import com.difisoft.htsconnection.socket.DataConnectionInfo;
import com.difisoft.htsconnection.socket.message.receive.LogInRcv;
import com.difisoft.htsconnection.socket.nonblocking.BaseHtsConnectionHandler;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.redis.RedisDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Service
public class HtsConnectionService {
    private static final Logger log = LoggerFactory.getLogger(HtsConnectionService.class);
    private static final String REDIS_KEY = "connectionInfo";

    private final AppConf appConf;
    private final RedisDao redisDao;
    private final SelectorService selectorService;

    public HtsConnectionService(AppConf appConf, RedisDao redisDao, SelectorService selectorService) {
        this.appConf = appConf;
        this.redisDao = redisDao;
        this.selectorService = selectorService;
    }

    public CompletableFuture<BaseHtsConnectionHandler> createConnection(
            AppConf.RealtimeAccountConf accountConf,
            Integer connectionIndex,
            Consumer<Throwable> disconnectEvent
    ) {
        CompletableFuture<LogInRcv> future = new CompletableFuture<>();
        BaseHtsConnectionHandler connectionHandler = createConnection(
                accountConf,
                connectionIndex,
                disconnectEvent,
                (r, e) -> {
                    if (e != null) {
                        future.completeExceptionally(e);
                    } else {
                        future.complete(r);
                    }
                    return null;
                }
        );
        return future.thenApply(r -> connectionHandler);
    }

    public BaseHtsConnectionHandler createConnection(
            AppConf.RealtimeAccountConf accountConf,
            Integer connectionIndex,
            Consumer<Throwable> disconnectEvent,
            BiFunction<LogInRcv, Throwable, Object> loginResultHandler
    ) {
        BaseHtsConnectionHandler connectionHandler = new BaseHtsConnectionHandler(
                connectionIndex == null ? accountConf.getName() : (String.format("%s-%d", accountConf.getName(), connectionIndex)),
                selectorService.get()
        );
        if (disconnectEvent != null) {
            connectionHandler.subscribeDisconnectEvent(disconnectEvent);
        }
        DataConnectionInfo dataConnectionInfo = null;
        if (appConf.isEnableStoreConnectionInfo()) {
            dataConnectionInfo = redisDao.get(getKey(accountConf.getUsername()), DataConnectionInfo.class);
        }
        if (dataConnectionInfo == null) {
            log.warn("{} there is no connection info for account. Will start from login", accountConf.getUsername());
            dataConnectionInfo = appConf.get(accountConf);
            DataConnectionInfo finalDataConnectionInfo = dataConnectionInfo;
            connectionHandler.startFromLogin(dataConnectionInfo).handle((r, e) -> {
                log.warn("{} Login finished! is_success: {}", accountConf.getUsername(), e == null);
                if (e == null) {
                    try {
                        DataConnectionInfo data = new DataConnectionInfo();
                        data.update(finalDataConnectionInfo);
                        data.setBranchCode1(r.getAttachingBranch().getValue());
                        data.setBranchCode2(r.getBranch().getValue());
                        data.setPersonalId(r.getSocialSecurityNumber().getValue());
                        data.setAgencyNumber(r.getAgencyNumber().getValue());
                        data.setDepartmentNumber(r.getDepartmentNumber().getValue());
                        redisDao.set(getKey(accountConf.getUsername()), data, 1800000L); // store in 30 mins
                    } catch (Exception ex) {
                        log.warn("fail to save data connection info to redis", ex);
                    }
                }
                loginResultHandler.apply(r, e);
                return null;
            });
        } else {
            log.warn("{} there is connection info for account. Will start from data connection", accountConf.getUsername());
            dataConnectionInfo.update(appConf.get(accountConf));
            connectionHandler.startFromData(dataConnectionInfo).handle((r, e) -> {
                loginResultHandler.apply(r, e);
                return null;
            });
        }
        return connectionHandler;
    }

    private String getKey(String account) {
        return String.format("%s_%s_%s", appConf.getClusterId(), REDIS_KEY, account.toUpperCase());
    }
}
