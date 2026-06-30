package com.difisoft.nhsv.admin.service.vietstock;

import com.difisoft.nhsv.admin.service.vietstock.context.VietStockEventDataContext;

public interface IVietStockEventStrategy {
    void process(VietStockEventDataContext context);
}
