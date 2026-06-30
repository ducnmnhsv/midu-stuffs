package com.techx.tradex.notification.services;

import java.io.IOException;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.databind.ObjectMapper;
// import com.neovisionaries.ws.client.WebSocketException;
// import com.neovisionaries.ws.client.WebSocketFrame;
import com.techx.tradex.common.exceptions.InvalidValueException;
import com.techx.tradex.common.model.notification.NotificationMessage;
import com.techx.tradex.common.model.notification.SocketClusterConfiguration;
import com.techx.tradex.common.utils.StringUtils;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.controllers.ResponseProcess;
import com.techx.tradex.notification.model.PublishData;

// import com.techx.tradex.notification.utils.SocketMinBinCodec;
// import io.github.sac.Ack;
// import io.github.sac.BasicListener;
// import io.github.sac.ReconnectStrategy;
// import io.github.sac.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SocketClusterService {
    private static final Logger log = LoggerFactory.getLogger(SocketClusterService.class);
    private RequestSender requestSender;
    private AppConf appConf;
    // private Socket socket;
    private ObjectMapper objectMapper;
    private String kafkaTopic;
    // private Ack publishAck = (String name, Object error, Object data) -> {
    //     log.info("data {} and error {}", data, error);
    //     if (error != null && !(error instanceof NullNode)) {
    //         if (error instanceof Throwable) {
    //             log.error("error on publishing to {} with error {}", name, error);
    //         } else {
    //             log.error("error on publishing to {} with error {} and type {}", name, error, error.getClass());
    //         }
    //     }
    // };

    @Autowired
    public SocketClusterService(AppConf appConf, ObjectMapper objectMapper, RequestSender requestSender) {
        this.requestSender = requestSender;
        this.appConf = appConf;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        if (appConf.getSocketCluster().isUsingKafka()) {
            this.kafkaTopic = this.appConf.getSocketCluster().getKafkaTopic();
            return;
        }
        String scheme = appConf.getSocketCluster().isSecure() ? "wss://" : "ws://";
        // this.socket = new Socket(scheme + appConf.getSocketCluster().getHostname() + ":" +
        //         appConf.getSocketCluster().getPort() + "/" + appConf.getSocketCluster().getPath());
        // if (appConf.getSocketCluster().isAutoReconnection()) {
        //     this.socket.setReconnection(new ReconnectStrategy().setDelay(600));
        // }
        // if (!appConf.getSocketCluster().isLogMessage()) {
        //     this.socket.disableLogging();
        // }
        // if (StringUtils.isEmpty(appConf.getSocketCluster().getCodec())
        //         || appConf.getSocketCluster().getCodec().equals(AppConf.SocketClusterConf.CODEC_MIN_BIN)) {
        //     this.socket.setCodec(new SocketMinBinCodec());
        // }
        // this.socket.setListener(new BasicListener() {
        //     @Override
        //     public void onConnected(Socket socket, Map<String, List<String>> headers) {
        //         log.info("SC Connected {}", headers);
        //     }

        //     @Override
        //     public void onDisconnected(Socket socket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) {
        //         log.warn("SC Disconnected by {}, server reason {}, client reason {}",
        //                 closedByServer ? "server" : "client",
        //                 serverCloseFrame != null ? serverCloseFrame.getCloseReason() : "",
        //                 clientCloseFrame != null ? clientCloseFrame.getCloseReason() : "");
        //     }

        //     @Override
        //     public void onConnectError(Socket socket, WebSocketException exception) {
        //         log.warn("SC connect error", exception);
        //     }

        //     @Override
        //     public void onAuthentication(Socket socket, Boolean status) {
        //         log.warn("SC authentication state changed {}", status);
        //     }

        //     @Override
        //     public void onSetAuthToken(String token, Socket socket) {
        //         log.warn("SC authentication data changed {}", token);
        //     }
        // });
        // this.socket.connect();
    }

    public void publish(String channel, Object data) {
        if (StringUtils.isEmpty(channel)) {
            log.error("channel is empty");
        }
        if (this.kafkaTopic != null) {
            PublishData obj = new PublishData();
            obj.setCn(channel);
            obj.setBd(data);
            try {
                this.requestSender.get().sendRequestNoResponse(this.kafkaTopic, "", obj);
            } catch (IOException e) {
                log.error("error on sending notification {} {}", channel, data);
            }
        } else {
            // this.socket.publish(channel, data, publishAck);
        }
    }

    @Async
    public void publish(NotificationMessage notificationMessage, ResponseProcess<Object> responseProcess) {
        try {
            if (notificationMessage.getTemplate() == null || notificationMessage.getTemplate().isEmpty()) {
                throw new InvalidValueException("template");
            }
            SocketClusterConfiguration request = notificationMessage.getConfiguration(objectMapper, SocketClusterConfiguration.class);
            if (StringUtils.isEmpty(request.getChannel())) {
                log.error("channel is empty");
            } else {
                notificationMessage.getTemplate().forEach((templateKey, data) -> {
                            if (this.kafkaTopic != null) {
                                PublishData obj = new PublishData();
                                obj.setCn(request.getChannel());
                                obj.setBd(data);
                                try {
                                    this.requestSender.get().sendRequestNoResponse(this.kafkaTopic, "", obj);
                                } catch (IOException e) {
                                    log.error("error on sending notification {}", notificationMessage);
                                }
                            } else {
                                // this.socket.publish(request.getChannel(), data, publishAck);
                            }
                        }
                );
            }
        } catch (Exception e) {
            log.error("error on publish sc {}", notificationMessage.getConfiguration(), e);
            if (responseProcess != null) {
                responseProcess.response(null, notificationMessage, e);
            }
        }
    }
}
