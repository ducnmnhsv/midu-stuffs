package com.difisoft.nhsv.admin.common;

import com.difisoft.nhsv.admin.domain.Holiday;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
public class HolidayConfiguration {

    private final AtomicReference<List<Holiday>> holidaysCache = new AtomicReference<>();

    @PostConstruct
    @Transactional
    public void init() {

    }

    public List<Holiday> get() {
        return holidaysCache.get();
    }

    public void set(List<Holiday> holiday) {
        holidaysCache.getAndSet(holiday);
    }
}
