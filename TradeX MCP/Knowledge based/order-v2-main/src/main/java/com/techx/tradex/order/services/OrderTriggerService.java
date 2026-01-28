package com.techx.tradex.order.services;

import com.difisoft.market.model.v2.db.SymbolQuote;
import com.difisoft.model.utils.LoopLinkedList;
import com.techx.tradex.order.configurations.AppConf;
import com.techx.tradex.order.dao.BridgeOrderDao;
import com.techx.tradex.order.model.db.StopOrder;
import com.techx.tradex.order.pricemonitor.OrderThreadHandler;
import com.techx.tradex.order.repositories.StopOrderRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderTriggerService {
    private final LoopLinkedList<OrderThreadHandler> handlers = new LoopLinkedList<>();
    private final Map<String, OrderThreadHandler> codeMapping = new ConcurrentHashMap<>();
    private final AppConf appConf;
    private final BridgeOrderDao bridgeOrderDao;
    private final StopOrderRepository stopOrderRepository;
    private final CacheService cacheService;


    @Autowired
    public OrderTriggerService(
            AppConf appConf,
            BridgeOrderDao bridgeOrderDao,
            StopOrderRepository stopOrderRepository,
            CacheService cacheService
    ) {
        this.appConf = appConf;
        this.bridgeOrderDao = bridgeOrderDao;
        this.stopOrderRepository = stopOrderRepository;
        this.cacheService = cacheService;
    }

    @PostConstruct
    public void init() {
        log.info("init threads {}", appConf.getNoThreadHandler());
        for (int i = 0; i < appConf.getNoThreadHandler(); i++) {
            handlers.add(new OrderThreadHandler(bridgeOrderDao, "OrderHandler-" + i, cacheService.getCacheSymbolInfo()));
        }
        // init stop order
        List<StopOrder> stopOrderList = stopOrderRepository.findTodayPendingStopOrder();
        Map<String, List<StopOrder>> orders = stopOrderList.stream().collect(Collectors.groupingBy(StopOrder::getCode));
        log.info("there is {} pending stop orders in db", orders.size());
        List<Map.Entry<String, List<StopOrder>>> sorted = orders.entrySet().stream().sorted(Comparator.comparingInt(v -> v.getValue().size())).collect(Collectors.toList());
        log.info("distribute order from db to threads {}", sorted.size());
        for (Map.Entry<String, List<StopOrder>> entry : sorted) {
            OrderThreadHandler handler = handlers.next();
            codeMapping.put(entry.getKey(), handler);
            handler.addStopOrders(entry);
        }
        log.info("starting threads");
        handlers.forEach(it -> new Thread(it).start());
    }

    public void receiveQuote(SymbolQuote quote) {
        String code = quote.getCode();
        OrderThreadHandler handler = codeMapping.get(code);
        if (handler != null) {
            handler.receiveQuote(quote);
        }
    }

    public void addOrder(StopOrder stopOrder) {
        String code = stopOrder.getCode();
        OrderThreadHandler handler = codeMapping.get(code);
        if (handler == null) {
            handler = handlers.next();
            codeMapping.put(code, handler);
        }
        handler.addStopOrder(stopOrder);
    }

    public void removeOrder(StopOrder stopOrder) {
        String code = stopOrder.getCode();
        OrderThreadHandler handler = codeMapping.get(code);
        if (handler == null) {
            return;
        }
        handler.removeStopOrder(stopOrder);
    }

    public void cleanAll() {
        for (OrderThreadHandler handler : handlers) {
            handler.cleanAll();
        }
    }
}
