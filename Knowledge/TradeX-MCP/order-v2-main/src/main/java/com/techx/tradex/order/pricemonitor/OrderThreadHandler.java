package com.techx.tradex.order.pricemonitor;

import com.difisoft.market.model.v2.db.SymbolInfo;
import com.difisoft.market.model.v2.db.SymbolQuote;
import com.difisoft.model.constants.SellBuyTypeEnum;
import com.techx.tradex.order.dao.BridgeOrderDao;
import com.techx.tradex.order.model.db.StopOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class OrderThreadHandler implements Runnable {
    private final BlockingDeque<Command> queues = new LinkedBlockingDeque<>();
    private final Map<String, List<StopOrder>> cacheStopOrder = new HashMap<>();
    private final Map<String, SymbolInfo> symbolInfoCache;
    private final BridgeOrderDao bridgeOrderDao;
    private final String name;

    public OrderThreadHandler(BridgeOrderDao bridgeOrderDao,
                              String name,
                              Map<String, SymbolInfo> symbolInfoCache) {
        this.bridgeOrderDao = bridgeOrderDao;
        this.symbolInfoCache = symbolInfoCache;
        this.name = name;
    }

    public void receiveQuote(SymbolQuote quote) {
        this.queues.add(new ReceiveQuoteCommand(quote));
    }

    public void addStopOrders(Map.Entry<String, List<StopOrder>> entries) {
        this.queues.add(new AddStopOrdersCommand(entries));
    }

    public void addStopOrder(StopOrder stopOrder) {
        this.queues.add(new AddStopOrderCommand(stopOrder));
    }

    public void removeStopOrder(StopOrder stopOrder) {
        this.queues.add(new RemoveStopOrderCommand(stopOrder));
    }

    public void cleanAll() {
        this.queues.add(new CleanAllCommand());
    }

    @Override
    public void run() {
        Thread.currentThread().setName(this.name);
        while (true) {
            try {
                Command command = null;
                try {
                    command = this.queues.poll(60L, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                if (command == null) {
                    continue;
                }
                try {
                    command.processCommand(this);
                } catch (Exception e) {
                    log.error("fail to handle command {}", command, e);
                }
            } catch (Exception e) {
                log.error("An error occur on loop", e);
            }
        }
    }

    interface Command {
        void processCommand(OrderThreadHandler handler);
    }

    @Data
    @AllArgsConstructor
    public static class AddStopOrdersCommand implements Command {
        private Map.Entry<String, List<StopOrder>> entries;

        @Override
        public void processCommand(OrderThreadHandler handler) {
            log.info("adding orders {}", entries.getValue().stream().map(StopOrder::getShortDescription).collect(Collectors.joining(";")));
            List<StopOrder> current = handler.cacheStopOrder.get(entries.getKey());
            if (current == null || current.isEmpty()) {
                handler.cacheStopOrder.put(entries.getKey(), entries.getValue());
            } else {
                current.addAll(entries.getValue());
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class AddStopOrderCommand implements Command {
        private StopOrder stopOrder;

        @Override
        public void processCommand(OrderThreadHandler handler) {
            log.info("adding order {}", stopOrder.getShortDescription());
            List<StopOrder> current = handler.cacheStopOrder.get(stopOrder.getCode());
            if (current == null || current.isEmpty()) {
                current = new ArrayList<>();
                current.add(stopOrder);
                handler.cacheStopOrder.put(stopOrder.getCode(), current);
            } else {
                current.add(stopOrder);
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class RemoveStopOrderCommand implements Command {
        private StopOrder stopOrder;

        @Override
        public void processCommand(OrderThreadHandler handler) {
            log.info("removing order {}", stopOrder.getShortDescription());
            List<StopOrder> current = handler.cacheStopOrder.get(stopOrder.getCode());
            if (!current.removeIf(f -> Objects.equals(f.getId(), stopOrder.getId()))) {
                log.warn("not found order to remove {}-{}", stopOrder.getCode(), stopOrder.getId());
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class ReceiveQuoteCommand implements Command {
        private SymbolQuote symbolQuote;

        @Override
        public void processCommand(OrderThreadHandler handler) {
            log.info("receiving quote {}-{}", symbolQuote.getCode(), symbolQuote.getLast());
            String code = symbolQuote.getCode();
            List<StopOrder> orders = handler.cacheStopOrder.get(code);
            if (orders == null || orders.isEmpty()) {
                return;
            }
            Iterator<StopOrder> it = orders.iterator();
            while (it.hasNext()) {
                StopOrder stopOrder = it.next();
                if (stopOrder.getStopPrice() > 0) {
                    if ((stopOrder.getSellBuyType().equals(SellBuyTypeEnum.BUY) && stopOrder.getStopPrice() - symbolQuote.getLast() <= 0)
                            || (stopOrder.getSellBuyType().equals(SellBuyTypeEnum.SELL) && stopOrder.getStopPrice() - symbolQuote.getLast() >= 0)) {
                        log.info("activeStopOrder {}", stopOrder.getShortDescription());
                        it.remove();
                        handler.bridgeOrderDao.placeRealOrder(stopOrder, handler.symbolInfoCache.get(code));
                    }
                } else {
                    it.remove();
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    public static class CleanAllCommand implements Command {

        @Override
        public void processCommand(OrderThreadHandler handler) {
            log.info("cleaning all orders");
            handler.cacheStopOrder.clear();
        }
    }
}
