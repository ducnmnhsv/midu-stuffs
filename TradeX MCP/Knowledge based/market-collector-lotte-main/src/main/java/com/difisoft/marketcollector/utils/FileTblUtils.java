package com.difisoft.marketcollector.utils;

import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.marketcollector.constants.Constants;
import com.difisoft.marketcollector.model.db.Symbol;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class FileTblUtils {
    public static void parseMasterTblFile(Map<String, Symbol> stockIndexMap, File file, boolean isEn) throws IOException {
        FileUtils.readLines(file, "UTF-8").forEach(line -> {
            if (StringUtils.isEmpty(line)) {
                return;
            }
            String exchange = line.substring(0, 2);
            String code = line.substring(2, 14).trim();
            String middle = line.substring(14, 20).trim();
            String name = line.substring(20).trim();

            Symbol symbol = stockIndexMap.get(code);
            if (symbol != null) {
                symbol.setName(name, isEn);
            } else {
                symbol = new Symbol();
                symbol.setExchange(exchange);
                symbol.setCode(code);
                symbol.setName(name, isEn);
                stockIndexMap.put(code, symbol);
            }

            if (contains(Constants.STOCK_EXCHANGES, exchange)) {
                symbol.setType(SymbolTypeEnum.STOCK);
            } else if (contains(Constants.CW_EXCHANGES, exchange)) {
                symbol.setType(SymbolTypeEnum.CW);
            } else if (contains(Constants.INDEX_EXCHANGES, exchange)) {
                symbol.setType(SymbolTypeEnum.INDEX);
                if (!isEn) {
                    symbol.setCode(name.replace(" ", "").replace("Index", ""));
                }
                if (symbol.getRefCode() == null) {
                    symbol.setRefCode(code);
                }
            } else if (contains(Constants.FUTURES_EXCHANGES, exchange)) {
                symbol.setType(SymbolTypeEnum.FUTURES);
            } else if (contains(Constants.BOND_EXCHANGES, exchange)) {
                symbol.setType(SymbolTypeEnum.BOND);
            }
        });
    }

    public static File decompressGzipFile(File gzipFile, String destination) throws IOException {
        File extractFile = new File(destination);
        try (FileInputStream fis = new FileInputStream(gzipFile);
             GZIPInputStream gis = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(extractFile)) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        }
        return extractFile;
    }

    private static boolean contains(String[] array, String match) {
        for (String s : array) {
            if (s.equals(match)) {
                return true;
            }
        }
        return false;
    }

    private static boolean finds(String[] array, String match) {
        for (String s : array) {
            if (s.equals(match)) {
                return true;
            }
        }
        return false;
    }
}
