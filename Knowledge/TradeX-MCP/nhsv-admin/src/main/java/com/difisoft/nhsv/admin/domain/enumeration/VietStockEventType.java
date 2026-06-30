package com.difisoft.nhsv.admin.domain.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VietStockEventType {
    CASHDIV("CASHDIV", 1, 13),
    BONUS_SHARES("BONUS_SHARES", 1, 14),
    STOCKDIV("STOCKDIV", 1, 15),
    ISSUE("ISSUE", 1, 16);

    private String code;
    private Integer typeId;
    private Integer channelID;

    public static VietStockEventType fromCode(String code) {
        for (VietStockEventType type : VietStockEventType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
