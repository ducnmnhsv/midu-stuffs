package com.difisoft.nhsv.admin.utils;

import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.nhsv.admin.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Objects;

@Slf4j
public class DateTimeUtil {

    private DateTimeUtil() {
    }

    public static Date getMinDateTime(String date, String pattern) {
        LocalDateTime localDateTime = toLocalDateTime(date, pattern).with(LocalTime.MIN);
        return toDate(localDateTime);
    }

    public static Date getMinTodayDateTime() {
        return toDate(LocalDateTime.now().with(LocalTime.MIN));
    }

    public static Date toDate(LocalDate localDate) {
        ZoneId zoneId = ZoneId.systemDefault();
        return Date.from(localDate.atStartOfDay().atZone(zoneId).toInstant());
    }

    public static Date toDate(LocalDateTime dateTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        return Date.from(dateTime.atZone(zoneId).toInstant());
    }

    public static LocalDateTime toLocalDateTime(String dateStr, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(dateStr, formatter).atStartOfDay();
    }

    public static Date atStartOfDay(Date date) {
        LocalDateTime localDateTime = toLocalDateTime(date);
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return toDate(startOfDay);
    }

    public static Date atEndOfDay(Date date) {
        LocalDateTime localDateTime = toLocalDateTime(date);
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MAX);
        return toDate(startOfDay);
    }

    public static Date minusDays(Date date, long days) {
        LocalDateTime localDateTime = toLocalDateTime(date);
        LocalDateTime beforeDate = localDateTime.minusDays(days);
        return toDate(beforeDate);
    }

    public static Date minusMonths(Date date, long months) {
        LocalDateTime localDateTime = toLocalDateTime(date);
        LocalDateTime beforeDate = localDateTime.minusMonths(months);
        return toDate(beforeDate);
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant()
            .atZone(ZoneId.of("UTC"))
            .toLocalDate();
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
    }

    public static boolean isSATURDAY(final LocalDate ld) {
        DayOfWeek day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
        return day == DayOfWeek.SATURDAY;
    }

    public static boolean isSUNDAY(final LocalDate ld) {
        DayOfWeek day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
        return day == DayOfWeek.SUNDAY;
    }

    public static boolean isMONDAY(final LocalDate ld) {
        DayOfWeek day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
        return day == DayOfWeek.MONDAY;
    }

    public static boolean isFRIDAY(final LocalDate ld) {
        DayOfWeek day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
        return day == DayOfWeek.FRIDAY;
    }

    public static boolean isTHURSDAY(final LocalDate ld) {
        DayOfWeek day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
        return day == DayOfWeek.THURSDAY;
    }

    public static boolean isWEDNESDAY(final LocalDate ld) {
        DayOfWeek day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
        return day == DayOfWeek.WEDNESDAY;
    }

    public static boolean isTUESDAY(final LocalDate ld) {
        DayOfWeek day = DayOfWeek.of(ld.get(ChronoField.DAY_OF_WEEK));
        return day == DayOfWeek.TUESDAY;
    }

    public static LocalDate tradingDateBefore() {

        final LocalDate ld = LocalDate.now();
        if (isMONDAY(ld)) {
            return ld.minusDays(3); // FRIDAY
        } else if (isSUNDAY(ld)) {
            return ld.minusDays(3); // THURSDAY
        } else if (isSATURDAY(ld)) {
            return ld.minusDays(2); // THURSDAY
        } else {
            return ld.minusDays(1);
        }
    }

    public static LocalDate tradingDatePresent() {

        final LocalDate ld = LocalDate.now();
        if (isSUNDAY(ld)) {
            return ld.minusDays(2); // FRIDAY
        } else if (isSATURDAY(ld)) {
            return ld.minusDays(1); // THURSDAY
        } else {
            return ld;
        }
    }

    public static ZonedDateTime toZonedDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    public static ZonedDateTime toZonedDateTime(LocalDate ld) {
        return ld.atStartOfDay(ZoneId.systemDefault());
    }

    public static ZonedDateTime toZonedDateTime(String str, String pattern) {
        if (str == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return toZonedDateTime(LocalDate.parse(str, formatter));
    }

    public static boolean isWorkingDate(final LocalDate zd) {
        return !isWeekend(zd) && !TradingDate.isHoliday(zd);
    }

    public static boolean isWeekend(final LocalDate zd) {
        DayOfWeek day = DayOfWeek.of(zd.get(ChronoField.DAY_OF_WEEK));
        return day == DayOfWeek.SUNDAY || day == DayOfWeek.SATURDAY;
    }

    public static ZonedDateTime stringToZoneDateTime(String dateString, String pattern, String type) {
        log.info("[stringToZoneDateTime] dateString: {}, pattern: {}, type: {}", dateString, pattern, type);
        try {
            if (StringUtils.isBlank(dateString) || StringUtils.isBlank(pattern) || StringUtils.isBlank(type)) {
                return null;
            }
            switch (type) {
                case Constants.DateTimeType.DATE:
                    return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern)).atStartOfDay(ZoneId.systemDefault());
                case Constants.DateTimeType.DATE_TIME:
                    return LocalDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern)).atZone(ZoneId.systemDefault());
                case Constants.DateTimeType.ZONE_DATE_TIME:
                    return ZonedDateTime.parse(dateString, DateTimeFormatter.ofPattern(pattern));
                default:
                    log.error("[stringToZoneDateTime] type: {} is not supported", type);
                    return null;
            }
        } catch (DateTimeParseException e) {
            log.error("[stringToZoneDateTime] dateString: {}, pattern: {}, type: {}, error: {}"
                , dateString, pattern, type, Util.objectToStringJsonIgnoreError(e.getStackTrace()));
            throw new GeneralException(MessageFormat.format(Constants.INPUT_DATE_FORMAT_IS_INVALID_MSG, dateString, pattern));
        }
    }

    public static ZonedDateTime toStartOfDay(ZonedDateTime zonedDateTime) {
        if (Objects.isNull(zonedDateTime)) {
            return null;
        }
        return zonedDateTime.toLocalDate().atTime(LocalTime.MIN).atZone(ZoneId.systemDefault());
    }

    public static ZonedDateTime toEndOfDay(ZonedDateTime zonedDateTime) {
        if (Objects.isNull(zonedDateTime)) {
            return null;
        }
        return zonedDateTime.toLocalDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault());
    }

    public static boolean isLastDayOfMonth(LocalDate month) {
        return month.lengthOfMonth() == month.getDayOfMonth();
    }

    public static boolean isLastDayOfYear(LocalDate year) {
        return year.lengthOfYear() == year.getDayOfYear();
    }

    public static boolean isLastDayOfWeek(LocalDate week) {
        return week.getDayOfWeek() == DayOfWeek.FRIDAY;
    }

    public static String zonedDateTimeToString(ZonedDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String localDateToString(LocalDate dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String localDateTimeToString(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }
}
