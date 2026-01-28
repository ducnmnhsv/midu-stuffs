package com.difisoft.marketcollector.model.db;

import com.difisoft.market.model.constant.SymbolTypeEnum;
import com.difisoft.marketcollector.constants.Constants;
import com.difisoft.marketcollector.constants.MarketTypeEnum;
import com.difisoft.model.utils.Pair;
import com.difisoft.model.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document(collection = "c_symbol")
@ToString
public class Symbol {
    @Id
    private String code;
    private String secCode;
    private String refCode;
    private String exchange;
    private String name;
    private String nameEn;
    private String marketType;
    private SymbolTypeEnum type;
    private String securitiesType;
    private Integer isHighlight;
    private Date createdAt = new Date();

    public boolean isValid() {
        return this.code != null && this.name != null && this.nameEn != null
                && this.exchange != null && this.type != null;
    }

    public void setName(String name, boolean isEn) {
        if (isEn) {
            this.nameEn = name;
        } else {
            this.name = name;
        }
    }

    public String getName() {
        if (StringUtils.isEmpty(this.name)) {
            this.name = this.getValidCode();
        }
        return this.name;
    }

    public String getNameEn() {
        if (StringUtils.isEmpty(this.nameEn)) {
            this.nameEn = this.getValidCode();
        }
        return this.nameEn;
    }

    @JsonIgnore
    public String getMarketType() {
        if (type == null) {
            return "";
        }
        switch (type) {
            case INDEX: {
                return Constants.INDEX_EXCHANGES_MAP.get(this.getExchange());
            }
            case STOCK: {
                Pair<String, String> pair = Constants.STOCK_EXCHANGES_MAP.get(this.getExchange());
                if (pair != null) {
                    return pair.getLeft();
                }
                return "";
            }
            case CW: {
                return Constants.CW_EXCHANGES_MAP.get(this.getExchange());
            }
            case FUTURES: {
                return Constants.FUTURES_EXCHANGES_MAP.get(this.getExchange());
            }
            default:
                return "";
        }
    }

    @JsonIgnore
    public String getSecuritiesType() {
        if (type == null) {
            return "";
        }
        if (this.type.equals(SymbolTypeEnum.STOCK)) {
            if (this.exchange.equals(MarketTypeEnum.HNX.getExchange()) || this.exchange.equals(MarketTypeEnum.UPCOM.getExchange())
                    || this.exchange.equals(MarketTypeEnum.HOSE.getExchange())) {
                return SymbolTypeEnum.STOCK.name();
            }
            if (this.exchange.equals(MarketTypeEnum.ETF_HOSE.getExchange())) {
                return SymbolTypeEnum.ETF.name();
            }
//            if (this.exchange.equals(MarketTypeEnum.FUND_HOSE.getExchange())) {
//                return SymbolTypeEnum.FUND.name();
//            }
        }
        return "";
    }

    @JsonIgnore
    public SymbolTypeEnum getType(SymbolTypeEnum defaultType) {
        if (type == null) {
            return defaultType;
        }
        if (this.type.equals(SymbolTypeEnum.STOCK)) {
            if (this.exchange.equals(MarketTypeEnum.ETF_HOSE.getExchange())) {
                return SymbolTypeEnum.ETF;
            }
            return SymbolTypeEnum.STOCK;
        }
        return this.type;
    }

    @JsonIgnore
    public String getValidCode() {
        return this.refCode != null ? this.refCode : this.code;
    }
}
