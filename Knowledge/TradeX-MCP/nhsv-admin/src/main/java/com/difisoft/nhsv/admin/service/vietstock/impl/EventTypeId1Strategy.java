package com.difisoft.nhsv.admin.service.vietstock.impl;

import com.difisoft.model.utils.DefaultUtils;
import com.difisoft.nhsv.admin.domain.StockEvent;
import com.difisoft.nhsv.admin.domain.enumeration.VietStockEventType;
import com.difisoft.nhsv.admin.service.vietstock.AbstractVietStockEventStrategy;
import com.difisoft.nhsv.admin.service.vietstock.response.IVietStockEventDto;
import com.difisoft.nhsv.admin.service.vietstock.response.VietStockEventPageResponse;
import com.difisoft.nhsv.admin.service.vietstock.response.impl.EventTypeID1Dto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service("eventTypeId1Strategy")
public class EventTypeId1Strategy extends AbstractVietStockEventStrategy {
    private final ObjectMapper objectMapper;

    public EventTypeId1Strategy(
        ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected VietStockEventPageResponse parseResponse(String responseString, VietStockEventType eventType) {
        List<Object> response = null;
        try {
            response = objectMapper.readValue(responseString, new TypeReference<List<Object>>() {});
        } catch (JsonProcessingException e) {
            return new VietStockEventPageResponse(new ArrayList<>(), 0);
        }

        if (response == null || response.size() != 2) {
            return new VietStockEventPageResponse(new ArrayList<>(), 0);
        }

        List<IVietStockEventDto> dtos = objectMapper.convertValue(response.get(0),
                new TypeReference<List<EventTypeID1Dto>>() {})
            .stream()
            .map(dto -> (IVietStockEventDto) dto)
            .collect(Collectors.toList());
        Integer total = objectMapper.convertValue(response.get(1), new TypeReference<List<Integer>>() {
        }).get(0);

        return new VietStockEventPageResponse(dtos, total);
    }

    @Override
    protected StockEvent toEvent(IVietStockEventDto dto, VietStockEventType eventType) {
        EventTypeID1Dto typedDto = (EventTypeID1Dto) dto;
        StockEvent event = new StockEvent();
        event.setId(this.getEventId(dto));
        event.setCode(dto.getCode());
        event.setType(eventType.getCode());
        event.setRate(this.vietStockSupport.convertRate(((EventTypeID1Dto) dto).getRate()));
        event.setEffectiveDate(Optional.ofNullable(typedDto.getGdkhqDate()).map(i -> Instant.ofEpochMilli(i)
                .atZone(DefaultUtils.VIETNAM_ID)).orElse(null));
        event.setExpiredDate(Optional.ofNullable(typedDto.getNdkcDate()).map(i -> Instant.ofEpochMilli(i)
                .atZone(DefaultUtils.VIETNAM_ID)).orElse(null));
        event.setSettlementDate(Optional.ofNullable(typedDto.getTime()).map(i -> Instant.ofEpochMilli(i)
                .atZone(DefaultUtils.VIETNAM_ID)).orElse(null));
        event.setEventNote(typedDto.getNote());
        return event;
    }
}
