package com.difisoft.marketcollector.services;

import com.difisoft.htsconnection.socket.LoginData;
import com.difisoft.marketcollector.configurations.AppConf;
import com.difisoft.marketcollector.model.db.Symbol;
import com.difisoft.marketcollector.socket.DownloadResourceConnectionHandler;
import com.difisoft.marketcollector.utils.CompletableUtil;
import com.difisoft.marketcollector.utils.FileTblUtils;
import com.difisoft.model.utils.DefaultUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@Slf4j
public class DownloadSymbolListService {
    private static final long STOCK_INDEX_MAP_LIFE_TIME = 14400000;

    private final SelectorService selectorService;
    private final AppConf appConf;

    private Map<String, Symbol> stockIndexMap;
    private long stockIndexMapUpdatedAt = 0;

    @Autowired
    public DownloadSymbolListService(SelectorService selectorService, AppConf appConf) {
        this.selectorService = selectorService;
        this.appConf = appConf;
    }

    public CompletableFuture<Map<String, Symbol>> downloadFuture(boolean useCache) {
        LoginData loginData = appConf.get(appConf.getAccountDownload());
        if (useCache && stockIndexMap != null &&
                stockIndexMapUpdatedAt > 0 &&
                System.currentTimeMillis() - this.stockIndexMapUpdatedAt < STOCK_INDEX_MAP_LIFE_TIME) {
            log.info("download resource use cache {}", useCache);
            return CompletableFuture.completedFuture(stockIndexMap);
        }
        final Map<String, Symbol> stockIndexMap = new HashMap<>();
        final List<String> files = new ArrayList<>();
        log.info("download resource new");
        DownloadResourceConnectionHandler downloadResourceConnectionHandler = new DownloadResourceConnectionHandler("downloadResource", selectorService.get(), (fileName) -> {
            if (!fileName.matches("master(_eng)?.tbl")) {
                return DownloadResourceConnectionHandler.Command.NOT_DOWNLOAD;
            }
            return DownloadResourceConnectionHandler.Command.DOWNLOAD;
        });
        CompletableFuture<Map<String, Symbol>> result = new CompletableFuture<>();
        List<String> fileNames;
        try {
            fileNames = downloadResourceConnectionHandler.start(loginData).join();
        } catch (CompletionException e) {
            log.error("download resource error", e.getCause());
            return CompletableUtil.exception(e.getCause());
        } catch (IOException e) {
            log.error("download resource connect error", e);
            return CompletableUtil.exception(e);
        }
        for (String fileName : fileNames) {
            if (fileName.matches("master(_eng)?.tbl")) {
                files.add(fileName);
                boolean isEn = fileName.contains("_eng");
                try {
                    log.info("reading file  {}", fileName);
                    Date date = new Date();
                    String extractFileName = FilenameUtils.getBaseName(fileName) + "extracted" + "_" + DefaultUtils.DATETIME_FORMAT().format(date) + ".txt";

                    File extractFile = FileTblUtils.decompressGzipFile(new File(fileName), extractFileName);
                    log.info("downloaded file: {}", extractFile.getAbsolutePath());
                    FileTblUtils.parseMasterTblFile(stockIndexMap, extractFile, isEn);
                    log.info("total stock/index added  {}", stockIndexMap.size());
                    if (files.size() == 2) {
                        log.info("finish download stock and index  {}", stockIndexMap.size());
                        this.stockIndexMap = stockIndexMap;
                        this.stockIndexMapUpdatedAt = System.currentTimeMillis();
                        result.complete(stockIndexMap);
                    }
                } catch (Exception e) {
                    log.error("Fail to parse or upload s3 master file", e);
                    result.completeExceptionally(e);
                }
            }
        }
        return result;
    }
}
