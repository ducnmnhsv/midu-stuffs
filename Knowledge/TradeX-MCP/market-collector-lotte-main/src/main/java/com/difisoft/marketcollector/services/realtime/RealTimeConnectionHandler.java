package com.difisoft.marketcollector.services.realtime;

import com.difisoft.htsconnection.exceptions.HtsErrorException;
import com.difisoft.htsconnection.socket.message.AutoRcv;
import com.difisoft.htsconnection.socket.message.receive.GeneratedClassRegistration;
import com.difisoft.htsconnection.socket.nonblocking.BaseHtsConnectionHandler;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.model.realtime.IndexUpdateData;
import com.difisoft.marketcollector.model.realtime.TransformData;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.utils.LoopLinkedList;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class RealTimeConnectionHandler implements Runnable {
     final RealTimeService realTimeService;
     final AppConf.RealtimeAccountConf accountConf;
     final List<String> codes; // sorted by most used first
     final Integer connectionIndex = 0;
     final String baseTransformPackageName;
     final BlockingQueue<Command> commands = new LinkedBlockingQueue<>();
    /**
     * connection handler
     */
     BaseHtsConnectionHandler connection;

     final LoopLinkedList<ThreadHandler> threads = new LoopLinkedList<>();
     final ConcurrentHashMap<String, ThreadHandler> codeThreadMap = new ConcurrentHashMap<>();
     final ConcurrentHashMap<Class<?>, ThreadHandler> classThreadMap = new ConcurrentHashMap<>();
    /**
     * control reconnection
     */
     long version;
     Long lastReceivePacket = null;
    Long lastSubscribe = null;

    public RealTimeConnectionHandler(
            RealTimeService realTimeService,
            AppConf.RealtimeAccountConf accountConf,
            List<String> codes,
            int delay
    ) {
        this.realTimeService = realTimeService;
        this.accountConf = accountConf;
        this.codes = codes;
        this.baseTransformPackageName = IndexUpdateData.class.getPackage().getName();
        this.commands.add(new StartOrRetry(delay));
        new Thread(this).start();
    }

    @Override
    public void run() {
        Thread.currentThread().setName(accountConf.getName() + "-" + connectionIndex);
        long currentConnectionVersion = 0;
        boolean currentConnectionIsStarting = false;
        int retryIndex = 0;

        while (true) { // run forever
            Command command = null;
            try {
                command = commands.poll(15, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (command == null) {
                if (!currentConnectionIsStarting) {
                    checkResubscribe();
                    checkReceivingTime();
                }
                continue;
            }
            if (command.type == CommandType.SUCCESS_CONNECT) {
                currentConnectionIsStarting = false;
                retryIndex = 0;
                log.warn("{}-{} connection success for index {}", accountConf.getName(), this.connectionIndex, retryIndex);
            } else if (command instanceof StartOrRetry startOrRetry) {
                if (startOrRetry.version < currentConnectionVersion) {
                    continue; // ignore
                }
                if (startOrRetry.delay != null) {
                    try {
                        Thread.sleep(startOrRetry.delay);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (command.type == CommandType.START) {
                    if (currentConnectionIsStarting) {
                        continue;
                    }
                    this.stop();
                    this.lastSubscribe = null;
                    this.lastReceivePacket = null;
                    currentConnectionVersion++;
                    this.version = currentConnectionVersion;
                    currentConnectionIsStarting = true;
                    start(retryIndex);
                } else if (command.type == CommandType.DISCONNECTED) {
                    currentConnectionIsStarting = false;
                    retryIndex++;
                    commands.add(new StartOrRetry(CommandType.START, startOrRetry.version));
                }
            }
        }
    }

     void start(int retryIndex) {
        int delay = 0;
        if (retryIndex > 2 * realTimeService.appConf.getRealtime().getMaxRetry()) {
            delay = 60000;
            log.error("{}-{} retry exceeded. will delay {} ms", accountConf.getName(), this.connectionIndex, delay);
        } else if (retryIndex > realTimeService.appConf.getRealtime().getMaxRetry()) {
            delay = 10000;
            log.info("{}-{} it's a retry extension. will delay {} ms!!!!", accountConf.getName(), this.connectionIndex, delay);
        } else if (retryIndex > 0) {
            delay = 1000;
            log.info("{}-{} it's a retry. will delay {} ms", accountConf.getName(), this.connectionIndex, delay);
        }

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (threads.isEmpty()) {
            for (int i = 0; i < accountConf.getNoOfThread(); i++) {
                LinkedBlockingQueue<ThreadHandler.Data> queue = new LinkedBlockingQueue<>();
                ThreadHandler thread = new ThreadHandler(
                        queue,
                        realTimeService.appConf,
                        realTimeService.cacheService,
                        realTimeService.createRequestSender()
                );
                threads.add(thread);
            }

            // codes is sorted already
            codes.forEach(code -> codeThreadMap.put(code, threads.next()));
            log.info("{}-{} start processing threads {}", accountConf.getName(), this.connectionIndex, threads.size());
            threads.forEach(thread -> {
                Thread t = new Thread(thread);
                t.setName(accountConf.getName() + "-" + this.connectionIndex + "-" + retryIndex);
                t.start();
            });
        }

        if (this.connection != null) {
            this.connection.closeConnections();
        }
        log.info("{}-{} will subscribe {}-{}", accountConf.getName(), this.connectionIndex, codes, codes.size());
        this.connection = this.realTimeService.htsConnectionService.createConnection(
                accountConf,
                this.connectionIndex,
                e -> { // disconnect event
                    if (this.realTimeService.isRunning) {
                        log.error("{}-{} connection is disconnected. Retrying..", accountConf.getName(), this.connectionIndex, e);
                        commands.add(new StartOrRetry(version, 3000));
                    } else {
                        log.error("{}-{} Realtime is not running. Will Stop!!!", accountConf.getName(), this.connectionIndex, e);
                    }
                },
                (c, e) -> { // data connection
                    if (e != null) {
                        if (e instanceof CompletionException) {
                            e = e.getCause();
                        }
                        log.error("{}-{} an error occur when trying to connect", accountConf.getName(), this.connectionIndex, e);
                        if (e.getCause() != null && e.getCause() instanceof GeneralException) {
                            Throwable source = ((GeneralException) e.getCause()).getSource();
                            if (source instanceof HtsErrorException) {
                                List<String> messageParams = ((HtsErrorException) source).getMessageParams();
                                if (messageParams != null && messageParams.size() > 1 && messageParams.get(1).trim().equals("127")) {
                                    log.error("{}-{} it's user configure problem: {}. will not retry", accountConf.getName(), this.connectionIndex, this.accountConf);
                                    return null;
                                }
                            }
                        }
                        this.connection.disconnected(null, null);
                    } else {
                        log.info("{}-{} connection is started", accountConf.getName(), this.connectionIndex);
                        log.warn("{}-{} will connect realtime", accountConf.getName(), this.connectionIndex);
                        this.connection.connectRealTime(true, this::receivePacket).handle((r, er) -> {
                            if (er != null) {
                                if (er instanceof CompletionException && er.getCause() != null) {
                                    er = er.getCause();
                                }
                                log.error("{}-{} an error occur when trying to connect realtime. Close connection and retrying..", accountConf.getName(), this.connectionIndex, er);
                                this.connection.disconnected(null, null);
                            } else {
                                commands.add(new Command(CommandType.SUCCESS_CONNECT));
                            }
                            return null;
                        });
                    }
                    return null;
                }
        );
    }

     void stop() {
        if (this.connection != null) {
            this.connection.closeConnections();
        }
    }


    void checkReceivingTime() {
        if (lastReceivePacket != null
                && (System.currentTimeMillis() - lastReceivePacket)
                        > realTimeService.appConf.getRealtime().getReceivePacketTimeout()
        ) {
            // will restart
            this.connection.disconnected(null, null);
        }
    }

    void checkResubscribe() {
        if (this.lastSubscribe != null &&
                accountConf.getResubscribeAfterMs() != null &&
                (System.currentTimeMillis() - this.lastSubscribe) > accountConf.getResubscribeAfterMs()) {
            this.startSubscribeAuto();
        }
    }

     void receivePacket(AutoRcv autoRcv) {
        if (autoRcv == null) {
            this.startSubscribeAuto();
            this.lastReceivePacket = System.currentTimeMillis();
        } else {
            this.lastReceivePacket = System.currentTimeMillis();
            if (autoRcv.getItems() == null || autoRcv.getItems().isEmpty()) {
                return;
            }
            if (realTimeService.appConf.isLogRealtimePacket()) {
                log.info("{}-{} got an real time packet {}", accountConf.getName(), this.connectionIndex, autoRcv.getItems());
            }
            Class<? extends TransformData<AutoRcv>> destClass;
            AppConf.SendOut sendOut;
            for (int i = 0; i < autoRcv.getItems().size(); i++) {
                AutoRcv item = autoRcv.getItems().get(i);
                // tem log
                Class<?> clazz = item.getClass();
                String className = clazz.getSimpleName();

                if (accountConf.getTopicMapping() != null) {
                    className = accountConf.getTopicMapping().get(className);
                }
                sendOut = this.realTimeService.sendOutMap.get(className);
                if (sendOut == null) {
                    log.warn("{}-{} cannot find a sending out config for class {}", accountConf.getName(), this.connectionIndex, clazz);
                    break;
                }
                if (realTimeService.destClassMap.containsKey(sendOut.getTransformTo())) {
                    destClass = realTimeService.destClassMap.get(sendOut.getTransformTo());
                    if (destClass == null) {
                        log.warn("{}-{} cannot find a dest class out config for class {}", accountConf.getName(), this.connectionIndex, clazz);
                        break;
                    }
                } else {
                    try {
                        destClass = (Class<? extends TransformData<AutoRcv>>) Class.forName(this.baseTransformPackageName + "." + sendOut.getTransformTo());
                        if (!TransformData.class.isAssignableFrom(destClass)) {
                            log.error("{}-{} transform class does not implement Transform {}", accountConf.getName(), this.connectionIndex, sendOut.getTransformTo());
                            realTimeService.destClassMap.put(sendOut.getTransformTo(), null);
                            break;
                        }
                    } catch (ClassNotFoundException e) {
                        log.error("{}-{} cannot find transform class for realtime data {}", accountConf.getName(), this.connectionIndex, sendOut.getTransformTo());
                        realTimeService.destClassMap.put(sendOut.getTransformTo(), null);
                        break;
                    }
                }

                String code = item.getCode().getValue();
                if (code != null) {
                    codeThreadMap.computeIfAbsent(code, k -> threads.next()).queue.add(new ThreadHandler.Data(item, clazz, destClass));
                } else {
                    classThreadMap.computeIfAbsent(item.getClass(), k -> threads.next()).
                            queue.add(new ThreadHandler.Data(item, clazz, destClass));
                }

            }
        }
    }

     void startSubscribeAuto() {
        log.info("{}-{} start register auto classes", accountConf.getName(), this.connectionIndex);
        accountConf.getTopics().forEach(topic -> {
            Class<? extends AutoRcv> clazz = GeneratedClassRegistration.getAutoRcvMap().get(topic);
            if (clazz != null && AutoRcv.class.isAssignableFrom(clazz)) {
                log.info("{}-{} subscribe auto class {} {} {}", accountConf.getName(), this.connectionIndex, clazz, topic, this.codes.size());
                if (realTimeService.appConf.getRealtime().getNumberCodesToRegister() >= 1) {
                    this.connection.subscribeSpecificAuto(this.codes, clazz, realTimeService.appConf.getRealtime().getNumberCodesToRegister());
                } else {
                    this.connection.subscribeAllItems(this.codes, clazz);
                }
            } else {
                log.error("{}-{} Auto topic class {} of topic {} is not derived class from AutoRcv", accountConf.getName(), this.connectionIndex, clazz, topic);
                System.exit(1);
            }
        });
        this.lastSubscribe = System.currentTimeMillis();
    }
}
