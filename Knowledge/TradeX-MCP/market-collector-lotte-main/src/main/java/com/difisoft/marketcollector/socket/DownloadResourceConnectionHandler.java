package com.difisoft.marketcollector.socket;

import com.difisoft.htsconnection.socket.LoginData;
import com.difisoft.htsconnection.socket.message.CNV;
import com.difisoft.htsconnection.socket.message.Constant;
import com.difisoft.htsconnection.socket.message.Packet;
import com.difisoft.htsconnection.socket.message.receive.BlockDataRcv;
import com.difisoft.htsconnection.socket.message.receive.DefinedTypeItem;
import com.difisoft.htsconnection.socket.message.receive.DefinedTypeRcv;
import com.difisoft.htsconnection.socket.message.receive.DownloadDataRcv;
import com.difisoft.htsconnection.socket.message.send.DefinedTypeSnd;
import com.difisoft.htsconnection.socket.message.send.DownloadDataSnd;
import com.difisoft.htsconnection.socket.nonblocking.BaseHtsConnectionHandler;
import com.difisoft.htsconnection.socket.nonblocking.Request;
import com.difisoft.htsconnection.socket.nonblocking.ResultWaiter;
import com.difisoft.htsconnection.socket.nonblocking.SocketClient;
import com.difisoft.model.exceptions.GeneralException;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class DownloadResourceConnectionHandler extends BaseHtsConnectionHandler {
    private static final Logger log = LoggerFactory.getLogger(DownloadResourceConnectionHandler.class);
    private static final int MAIN_REQUEST_KEY = -3;
    private static final int DEFINED_TYPE_RCV = -4;
    private static final int DATA_RCV = -5;

    private final List<DefinedTypeItem> items = new ArrayList<>();
    private final Function<String, Command> fileController;

    private List<String> fileNames;
    private String currentFileName;
    private Command currentCommand;
    private FileOutputStream fos;
    private CompletableFuture<List<String>> publishSubject;

    public DownloadResourceConnectionHandler(String connectionId, Selector selector, Function<String, Command> fileController) {
        super(connectionId, selector);
        this.fileController = fileController;
    }

    public CompletableFuture<List<String>> start(LoginData loginData) throws IOException {
        this.loginData = loginData;
        this.fileNames = new ArrayList<>();
        publishSubject = new CompletableFuture<>();
        requestKeyMap.put(MAIN_REQUEST_KEY, new ResultWaiter<>(publishSubject));
        log.info("create connection");
        this.login = new SocketClient(this.connectionId, SocketClient.Type.LOGIN, this.selector, this.loginData.getHost(), this.loginData.getLoginPort(), this, this.loginData.getBackupHosts());
        this.login.start();
        return publishSubject;
    }

    @Override
    public void connected(SocketClient socketClient, boolean success) {
        log.info("connected {}", success);
        if (success) {
            initBeforeLogin();
        }
    }

    private void initBeforeLogin() {
        log.info("initBeforeLogin");
        DefinedTypeSnd snd = new DefinedTypeSnd();
        snd.getSecCode().setValue(this.loginData.getSecCode());
        snd.getMediaType().setValue(loginData.getMediaType());
        snd.getLanguage().setValue(Constant.Language);
        this.sendMessageFuture(new Request<>(snd, new CompletableFuture<>(), DefinedTypeRcv.class, 18000), DEFINED_TYPE_RCV).handle(
                (data, err) -> {
                    if (err != null) {
                        if (err instanceof TimeoutException) {
                            return null;
                        }
                        throwResponse(new GeneralException().source(err), MAIN_REQUEST_KEY);
                    }
                    return null;
                }
        );
    }


    private void downloadResource(DefinedTypeItem item) {
        DownloadDataSnd snd = new DownloadDataSnd();
        snd.getSeqNo().setValue(item.getSeqNo().getValue());
        this.sendMessageFuture(new Request<>(snd, new CompletableFuture<>(), DownloadDataRcv.class, 180000), DATA_RCV).handle(
                (data, err) -> {
                    if (err != null) {
                        throwResponse(new GeneralException().source(err), MAIN_REQUEST_KEY);
                    }
                    return null;
                }
        );
    }

    private void nextFile() {
        if (currentCommand == Command.DOWNLOAD_THEN_FINISH) {
            log.info("{} finish download needed file!", this.connectionId);
            this.publishSubject.complete(this.fileNames);
            this.disconnected(null, null);
        }
        if (!this.items.isEmpty()) {
            DefinedTypeItem currentItem = this.items.get(this.items.size() - 1);
            this.currentFileName = FilenameUtils.getName(currentItem.getFilePath().getValue());
            if (this.fileController != null) {
                currentCommand = this.fileController.apply(this.currentFileName);
                if (currentCommand == Command.NOT_DOWNLOAD) {
//                    log.info("{} file {} will be ignore", this.connectionId, this.currentFileName);
                    this.items.remove(this.items.size() - 1);
                    nextFile();
                    return;
                }
            }
            log.info("{} will download file {}", this.connectionId, this.currentFileName);
            this.downloadResource(currentItem);
        } else {
            this.publishSubject.complete(this.fileNames);
            this.disconnected(null, null);
        }
    }

    @Override
    protected void processMessage(Packet packet) {
        if (packet instanceof BlockDataRcv) {
//            log.info("{} processMessage block data: {}-{}", this.connectionId, ((BlockDataRcv) packet).getSeqNo(), ((BlockDataRcv) packet).getCount());
        } else {
//            log.info("{} processMessage: {}", this.connectionId, packet);
        }
        packet.setConnectionHandler(this);

        if (packet instanceof DefinedTypeRcv) {
            if (packet.isError()) {
                this.throwResponse(new GeneralException(packet.getClass() + "-" + packet.getErrCode() + "-" + packet.getErrMessage()), MAIN_REQUEST_KEY);
                return;
            }
            if (((DefinedTypeRcv) packet).getCont().getValue() == 11) {
                this.callbackResponseByRequestKey(packet, DEFINED_TYPE_RCV);
                this.nextFile();
            } else {
                this.items.addAll(((DefinedTypeRcv) packet).getFiles());
            }

        } else if (packet instanceof DownloadDataRcv) {
            if (packet.isError()) {
                this.throwResponse(new GeneralException(packet.getClass() + "-" + packet.getErrCode() + "-" + packet.getErrMessage()), MAIN_REQUEST_KEY);
            }
            File currentFile = new File(this.currentFileName);
            try {
                this.fos = new FileOutputStream(currentFile);
//                log.info("{} create output stream for file {}", this.connectionId, this.currentFileName);
            } catch (IOException e) {
                throw new GeneralException().source(e);
            }
        } else if (packet instanceof BlockDataRcv) {
            if (packet.isError()) {
                this.throwResponse(new GeneralException(packet.getClass() + "-" + packet.getErrCode() + "-" + packet.getErrMessage()), MAIN_REQUEST_KEY);
            }
            try {
//                log.info("{} write block to file {}", this.connectionId, this.currentFileName);
                fos.write(((BlockDataRcv) packet).getData());
            } catch (IOException e) {
                throw new GeneralException().source(e);
            }
        } else if (packet instanceof CNV) {
            if (packet.isError()) {
                throwResponse(new GeneralException(packet.getClass() + "-" + packet.getErrCode() + "-" + packet.getErrMessage()), packet.getRequestKey().getValue());
                return;
            }
            if (((CNV) packet).getTypeID().getValue() == 121) {
                log.info("{} finish download file {}", this.connectionId, this.currentFileName);
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new GeneralException().source(e);
                }
                this.fileNames.add(this.currentFileName);
                this.items.remove(this.items.size() - 1);
                this.callbackResponseByRequestKey(packet, DATA_RCV);
                this.nextFile();
            }
        } else {
            if (packet.getRequestKey().getValue() == 0) {
                return;
            }
            this.callbackResponse(packet, (int) packet.getRequestKey().getValue());
        }
    }

    public enum Command {
        NOT_DOWNLOAD, DOWNLOAD, DOWNLOAD_THEN_FINISH;
    }
}
