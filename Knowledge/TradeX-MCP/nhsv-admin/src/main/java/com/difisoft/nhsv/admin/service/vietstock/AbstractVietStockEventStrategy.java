package com.difisoft.nhsv.admin.service.vietstock;

import com.difisoft.nhsv.admin.config.AppConf;
import com.difisoft.nhsv.admin.domain.StockEvent;
import com.difisoft.nhsv.admin.domain.enumeration.VietStockEventType;
import com.difisoft.nhsv.admin.service.vietstock.context.VietStockEventDataContext;
import com.difisoft.nhsv.admin.service.vietstock.response.IVietStockEventDto;
import com.difisoft.nhsv.admin.service.vietstock.response.VietStockEventPageResponse;
import com.difisoft.nhsv.admin.service.vietstock.resquest.VietStockAuthData;
import com.difisoft.nhsv.admin.service.vietstock.resquest.VietStockEventQueryRequest;
import com.difisoft.nhsv.admin.utils.DateFormatUtil;
import com.techx.tradex.common.exceptions.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractVietStockEventStrategy implements IVietStockEventStrategy {
    @Autowired
    protected VietStockSupport vietStockSupport;

    @Autowired
    protected AppConf appConf;

    @Override
    public void process(VietStockEventDataContext context) {
        // 1. Build request for VietStock API
        this.buildRequest(context);

        // 2. Fetch data from VietStock API
        this.fetchData(context);

        // 3. Parse responses to event entities
        this.convert(context);
    }

    protected void buildRequest(VietStockEventDataContext context) {
        // Get the event type
        VietStockEventType eventType = context.getEventType();
        // Build request based on event type
        Map<String, String> input = this.appConf.getVietStock().getEvent().getInput().get(eventType.name());

        ZonedDateTime from = context.getFromDate();
        ZonedDateTime to = Optional.ofNullable(context.getToDate())
            .orElse(from.plusDays(appConf.getVietStock().getDayStep()));
        String fDate = DateFormatUtil.getDateString(Date.from(from.toInstant()), "yyyy-MM-dd");
        String tDate = DateFormatUtil.getDateString(Date.from(to.toInstant()), "yyyy-MM-dd");

        // Get auth data from cache
        VietStockAuthData authData = this.vietStockSupport.getAuthDataFromCache();

        VietStockEventQueryRequest request = VietStockEventQueryRequest.builder()
            .eventTypeID(input.get("eventTypeID"))
            .channelID(input.get("channelID"))
            .catID(input.get("catID"))
            .fDate(fDate)
            .tDate(tDate)
            .cookies(authData.getCookies())
            .requestToken(authData.getRequestToken())
            .build();

        context.setRequest(request);
    }

    protected void fetchData(VietStockEventDataContext context) {
        VietStockEventQueryRequest request = context.getRequest();
        List<IVietStockEventDto> dtos = new ArrayList<>();
        AtomicInteger currentPage = new AtomicInteger(1);

        // Retrieve all pages of data
        while (true) {
            request.setPage(currentPage.get());
            try {
                // Call VietStock API to get event page
                String responseString = this.vietStockSupport.getEventPage(request);

                // If no response, skip
                if (StringUtils.isEmpty(responseString)) {
                    break;
                }
                log.debug("Raw response: {}", responseString);

                // Parse response to dto list and total records
                VietStockEventPageResponse pageResponse = this.parseResponse(responseString, context.getEventType());

                dtos.addAll(pageResponse.getList());

                if (pageResponse.getList().get(pageResponse.getList().size() - 1).getRow() >= pageResponse.getTotal()) {
                    break;
                }
                currentPage.set(currentPage.incrementAndGet());
            } catch (Exception e) {
                throw new GeneralException("Failed to fetch data from VietStock API: " + e.getMessage());
            }
        }

        context.setDtos(dtos);
    }

    protected void convert(VietStockEventDataContext context) {
        // Get the dtos from context
        List<IVietStockEventDto> dtos = context.getDtos();
        List<StockEvent> events = dtos.stream()
            .map(dto -> this.toEvent(dto, context.getEventType()))
            .collect(Collectors.toList());

        // Set the events to context
        context.setEvents(events);
    }

    protected String getEventId(IVietStockEventDto dto) {
        return String.format("%s_%s", dto.getEventId(), dto.getCode());
    }

    protected abstract VietStockEventPageResponse parseResponse(String responseString, VietStockEventType eventType);

    // Convert dto list to event entities
    protected abstract StockEvent toEvent(IVietStockEventDto dto, VietStockEventType eventType);
}
