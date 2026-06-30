package com.difisoft.nhsv.admin.utils;


import com.difisoft.nhsv.admin.service.impl.HolidayCustomServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.difisoft.nhsv.admin.utils.DateTimeUtil.isSATURDAY;
import static com.difisoft.nhsv.admin.utils.DateTimeUtil.isSUNDAY;


public class TradingDate {

    private static HolidayCustomServiceImpl holidayCustomService;

    private TradingDate() {
        holidayCustomService = ApplicationContextProvider.getBean(HolidayCustomServiceImpl.class);
    }

    private static void create() {
        if (holidayCustomService == null) {
            new TradingDate();
        }
    }

    public static LocalDate adjustHolidayDown(LocalDate date) {
        create();
        while (holidayCustomService.isHoliday(date)) {
            date = date.minusDays(1);
            if (isSATURDAY(date)) {
                date = date.minusDays(1); // FRIDAY
            } else if (isSUNDAY(date)) {
                date = date.minusDays(2); // FRIDAY
            }
        }
        return date;
    }

    public static LocalDateTime adjustHolidayDown(LocalDateTime dateTime) {
        LocalDate date = LocalDate.from(dateTime);
        while (holidayCustomService.isHoliday(date)) {
            dateTime = dateTime.minusDays(1);
            date = date.minusDays(1);
            if (isSATURDAY(date)) {
                dateTime = dateTime.minusDays(1);
                date = date.minusDays(1);
            } else if (isSUNDAY(date)) {
                dateTime = dateTime.minusDays(2);
                date = date.minusDays(2);
            }
        }
        return dateTime;
    }

    public static LocalDate adjustHolidayTo(LocalDate date) {
        create();
        while (holidayCustomService.isHoliday(date)) {
            date = date.plusDays(1);
            if (isSATURDAY(date)) {
                date = date.plusDays(2); // MONDAY
            } else if (isSUNDAY(date)) {
                date = date.plusDays(1); // MONDAY
            }
        }
        return date;
    }

    public static LocalDate ajustHolidayToIntervel(LocalDate date, int day) {
        create();
        while (day > 0) {
            date = date.plusDays(1);
            if (!isSATURDAY(date) && !isSUNDAY(date) && !holidayCustomService.isHoliday(date)) {
                day = day - 1;
            }
        }
        return date;
    }


    public static LocalDate adjustHolidayDownIntervel(LocalDate date, int day) {
        create();
        while (day > 0) {
            date = date.minusDays(1);
            if (!isSATURDAY(date) && !isSUNDAY(date) && !holidayCustomService.isHoliday(date)) {
                day = day - 1;
            }
        }
        return date;
    }


    public static LocalDate adjustDown(LocalDate date) {
        create();
        date = adjustHolidayDown(date);
        if (isSATURDAY(date)) {
            date = date.minusDays(1); // FRIDAY
        } else if (isSUNDAY(date)) {
            date = date.minusDays(2); // FRIDAY
        }
        date = adjustHolidayDown(date);

        return date;
    }

    public static LocalDateTime adjustDown(LocalDateTime date) {
        create();
        date = adjustHolidayDown(date);
        if (isSATURDAY(LocalDate.from(date))) {
            date = date.minusDays(1); // FRIDAY
        } else if (isSUNDAY(LocalDate.from(date))) {
            date = date.minusDays(2); // FRIDAY
        }
        date = adjustHolidayDown(date);

        return date;
    }

    public static LocalDate adjustTo(LocalDate date) {
        create();
        date = adjustHolidayTo(date);
        if (isSATURDAY(date)) {
            date = date.plusDays(2); // MONDAY
        } else if (isSUNDAY(date)) {
            date = date.plusDays(1); // MONDAY
        }
        date = adjustHolidayTo(date);

        return date;
    }

    /**
     * @param date is a working date
     * @return
     */
    public static LocalDate minusOne(LocalDate date) {
        LocalDate tradingDate = adjustDown(date.minusDays(1));
        return tradingDate;
    }

    /**
     * @param date is a working date
     * @return
     */
    public static LocalDate minusTwo(LocalDate date) {
        LocalDate tradingDate = minusOne(date);
        return minusOne(tradingDate);
    }

    /**
     * @param date is a working date
     * @return
     */
    public static LocalDate minusDays(LocalDate date, long daysToSubtract) {
        LocalDate tradingDate = date;
        for (int i = 0; i < daysToSubtract; i++) {
            tradingDate = minusOne(tradingDate);
        }
        return tradingDate;
    }

    /**
     * @param date is a working date
     * @return
     */
    public static LocalDate plusOne(LocalDate date) {
        return adjustTo(date.plusDays(1));
    }

    /**
     * @param date is a working date
     * @return
     */
    public static LocalDate plusTwo(LocalDate date) {
        LocalDate tradingDate = plusOne(date);
        return plusOne(tradingDate);
    }

    /**
     * @param date is a working date
     * @return
     */
    public static LocalDate plusDays(LocalDate date, long daysToAdd) {
        LocalDate tradingDate = date;
        for (int i = 0; i < daysToAdd; i++) {
            tradingDate = plusOne(tradingDate);
        }
        return tradingDate;
    }

    public static LocalDate now() {
        return adjustDown(LocalDate.now());
    }

    public static LocalDate parse(String input, String format) {
        LocalDate date = LocalDate.parse(input, DateTimeFormatter.ofPattern(format));
        return adjustDown(date);
    }

    public static LocalDate minusOne() {
        LocalDate tradingDate = TradingDate.now();
        return TradingDate.minusDays(tradingDate, 1);
    }

    public static LocalDate minusOneWeek() {
        LocalDate tradingDate = TradingDate.now();
        return TradingDate.minusWeeks(tradingDate, 1);
    }

    public static LocalDate minusWeeks(LocalDate date, long weeksToSubtract) {
        LocalDate _date = adjustDown(date);
        return adjustDown(_date.minusWeeks(weeksToSubtract));
    }

    public static LocalDate minusOneMonth() {
        LocalDate tradingDate = TradingDate.now();
        return TradingDate.minusMonths(tradingDate, 1);
    }

    public static LocalDate minusMonths(LocalDate date, long monthsToSubtract) {
        LocalDate _date = adjustDown(date);
        return adjustDown(_date.minusMonths(monthsToSubtract));
    }

    public static LocalDate minusOneYear() {
        LocalDate tradingDate = TradingDate.now();
        return TradingDate.minusYears(tradingDate, 1);
    }

    public static LocalDate minusYears(LocalDate date, long yearsToSubtract) {
        LocalDate _date = adjustDown(date);
        return adjustDown(_date.minusYears(yearsToSubtract));
    }

    public static Long between(LocalDate from, LocalDate to) {
        LocalDate holdingDays = from;
        Long count = 0L;
        while (!holdingDays.isAfter(to)) {
            holdingDays = plusOne(holdingDays);
            count++;
        }
        return count;
    }

    public static boolean isHoliday(LocalDate date) {
        create();
        return holidayCustomService.isHoliday(date);
    }
}
