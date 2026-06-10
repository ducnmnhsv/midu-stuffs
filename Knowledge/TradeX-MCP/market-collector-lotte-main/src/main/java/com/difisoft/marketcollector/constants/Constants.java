package com.difisoft.marketcollector.constants;

import com.difisoft.model.utils.Pair;

import java.util.HashMap;
import java.util.Map;

public interface Constants {
    String FIX_DEFAULT_CURRENCY = "VND";
    String DEFAULT_TIME = "120000";

    String[] STOCK_EXCHANGES = {"01", "11", "31", "03", "04"}; // 01: HOSE, 11: HNX, 31: UPCOM, 03: FUND_HOSE, 04: ETF_HOSE
    Map<String, Pair<String, String>> STOCK_EXCHANGES_MAP = new HashMap<>() {{
        put(STOCK_EXCHANGES[0], new Pair<>("HOSE", "STOCK"));
        put(STOCK_EXCHANGES[1], new Pair<>("HNX", "STOCK"));
        put(STOCK_EXCHANGES[2], new Pair<>("UPCOM", "STOCK"));
        put(STOCK_EXCHANGES[3], new Pair<>("HOSE", "FUND"));
        put(STOCK_EXCHANGES[4], new Pair<>("HOSE", "ETF"));
    }};
    String[] BOND_EXCHANGES = {"12"};
    String[] INDEX_EXCHANGES = {"09", "19", "39"};
    Map<String, String> INDEX_EXCHANGES_MAP = new HashMap<>() {{
        put(INDEX_EXCHANGES[0], "HOSE");
        put(INDEX_EXCHANGES[1], "HNX");
        put(INDEX_EXCHANGES[2], "UPCOM");
    }};
    String[] FUTURES_EXCHANGES = {"15"};
    Map<String, String> FUTURES_EXCHANGES_MAP = new HashMap<>() {{
        put(FUTURES_EXCHANGES[0], "HNX");
    }};
    String[] CW_EXCHANGES = {"07"};
    Map<String, String> CW_EXCHANGES_MAP = new HashMap<>() {{
        put(CW_EXCHANGES[0], "HOSE");
    }};
    Integer DEFAULT_HIGHLIGHT_NUMBER = 1000;
    Integer MAX_RETRY = 10;
}
