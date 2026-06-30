package com.difisoft.nhsv.admin.domain.enumeration;

import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.nhsv.admin.config.Messages;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ProfitLossPeriodEnum {
    ONE_DAY("1D"), ONE_WEEK("1W"), ONE_MONTH("1M"), THREE_MONTH("3M"), ONE_YEAR("1Y");

    private final String key;

    ProfitLossPeriodEnum(String key) {
        this.key = key;
    }

    public static List<String> keyList() {
        return Arrays.stream(ProfitLossPeriodEnum.values()).map(ProfitLossPeriodEnum::getKey).collect(Collectors.toList());
    }

    public static ProfitLossPeriodEnum findEnumByKey(String key) {
        return Arrays.stream(ProfitLossPeriodEnum.values())
            .filter(x -> StringUtils.equals(x.getKey(), key))
            .findFirst().orElseThrow(() -> new GeneralException(MessageFormat.format(Messages.PROFIT_LOSS_PERIOD_TYPE_ENUM_NOT_FOUND, key)));
    }

    public String getKey() {
        return key;
    }
}
