package com.difisoft.nhsv.admin.service.vietstock;

import com.difisoft.nhsv.admin.domain.enumeration.VietStockEventType;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Service("DefaultVietStockEventStrategyFactory")
public class DefaultVietStockEventStrategyFactory implements IVietStockEventStrategyFactory{
    private static final Map<Integer, String> TYPE_ID_TO_STRATEGY;

    static {
        Map<Integer, String> map = new HashMap<>();
        map.put(1, "eventTypeId1Strategy");
        TYPE_ID_TO_STRATEGY = Collections.unmodifiableMap(map);
    }

    private final Map<String, IVietStockEventStrategy> eventStrategyGroup;

    // Mapping between event type and strategy
    private Map<VietStockEventType, IVietStockEventStrategy> eventStrategyMap;

    public DefaultVietStockEventStrategyFactory(Map<String, IVietStockEventStrategy> eventStrategyGroup) {
        this.eventStrategyGroup = eventStrategyGroup;
        initEventStrategyMap();
    }

    // Initialize the event strategy map
    private void initEventStrategyMap() {
        eventStrategyMap = new EnumMap<>(VietStockEventType.class);

        for (VietStockEventType eventType : VietStockEventType.values()) {
            String strategyName = TYPE_ID_TO_STRATEGY.get(eventType.getTypeId());
            IVietStockEventStrategy strategy = eventStrategyGroup.get(strategyName);
            eventStrategyMap.put(eventType, strategy);
        }
    }


    @Override
    public IVietStockEventStrategy getEventStrategy(VietStockEventType eventType) {
        return eventStrategyMap.get(eventType);
    }
}
