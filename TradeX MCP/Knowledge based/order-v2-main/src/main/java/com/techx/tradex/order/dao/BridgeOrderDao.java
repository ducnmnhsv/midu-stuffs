package com.techx.tradex.order.dao;

import com.difisoft.market.model.v2.db.SymbolInfo;
import com.techx.tradex.order.model.db.ProfitLossOrder;
import com.techx.tradex.order.model.db.StopOrder;
import com.techx.tradex.order.model.db.TrailingOrder;
import com.techx.tradex.order.model.response.MasBosOrderCancelResponse;
import com.techx.tradex.order.model.response.MasBosOrderPlaceResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Using to execute real order
 */
@Service
public interface BridgeOrderDao {
    MasBosOrderPlaceResponse placeRealOrderSync(ProfitLossOrder order, SymbolInfo symbolInfo) throws IOException, ExecutionException, InterruptedException;

    List<MasBosOrderCancelResponse> cancelRealOrderSync(ProfitLossOrder profitLossOrder) throws IOException, ExecutionException, InterruptedException;

    void placeRealOrder(StopOrder stopOrder, SymbolInfo symbolInfo);

    ProfitLossOrder placeRealProfitLossOrderSync(ProfitLossOrder order, SymbolInfo symbolInfo);

    void placeRealOrder(ProfitLossOrder order, SymbolInfo symbolInfo);

    void placeRealOrder(TrailingOrder trailingOrder, SymbolInfo symbolInfo);
}
