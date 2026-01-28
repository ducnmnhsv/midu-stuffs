package com.difisoft.marketcollector.services;

import com.difisoft.htsconnection.socket.nonblocking.BaseHtsConnectionHandler;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.model.utils.Wrapper;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class DownloadAccountService extends TimerTask {
    private final RealTimeDataListenerService realTimeDataListenerService;
    private final HtsConnectionService htsConnectionService;
    private final AppConf appConf;
    private final Wrapper<BaseHtsConnectionHandler> connection = new Wrapper<>(null);
    private final AtomicInteger totalUsing = new AtomicInteger(0);

    public DownloadAccountService(
            RealTimeDataListenerService realTimeDataListenerService,
            HtsConnectionService htsConnectionService,
            AppConf appConf
    ) {
        this.realTimeDataListenerService = realTimeDataListenerService;
        this.htsConnectionService = htsConnectionService;
        this.appConf = appConf;
    }

    @Override
    public void run() {
        if (this.totalUsing.get() <= 0) {
            if (connection.getV() != null) {
                connection.getV().closeConnections();
                connection.setV(null);
            }
        }
    }

    public ConnectionController getConnection() {
        if (connection.getV() != null) {
            this.totalUsing.incrementAndGet();
            return new ConnectionController(connection.getV(), this, true);
        }
        this.connection.setV(htsConnectionService.createConnection(appConf.getAccountDownload(), null, null).join());
        this.totalUsing.incrementAndGet();
        return new ConnectionController(this.connection.getV(), this, true);
    }

    private void release() {
        if (this.totalUsing.decrementAndGet() <= 0) {
            try {
                new Timer().schedule(this, 5000L);
            } catch (IllegalStateException e) {
                // swallow
            }
        }
    }

    public static class ConnectionController {
        @Getter
        private final BaseHtsConnectionHandler connectionHandler;
        private final DownloadAccountService downloadAccountService;
        private final boolean relesable;

        public ConnectionController(BaseHtsConnectionHandler connectionHandler, DownloadAccountService downloadAccountService, boolean relesable) {
            this.connectionHandler = connectionHandler;
            this.downloadAccountService = downloadAccountService;
            this.relesable = relesable;
        }

        public void release() {
            if (relesable) {
                downloadAccountService.release();
            }
        }
    }
}
