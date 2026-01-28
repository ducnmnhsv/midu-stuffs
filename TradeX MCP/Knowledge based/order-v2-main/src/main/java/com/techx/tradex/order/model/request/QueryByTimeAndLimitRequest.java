package com.techx.tradex.order.model.request;

import com.difisoft.model.requests.DataRequest;
import com.difisoft.model.utils.DateUtils;
import com.difisoft.model.utils.DefaultUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.techx.tradex.order.constants.Constants;
import com.techx.tradex.order.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.techx.tradex.order.services.RequestSender.log;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryByTimeAndLimitRequest extends DataRequest {
    protected Integer fetchCount;
    protected String fromDate;
    protected String toDate;

    @AllArgsConstructor
    @Data
    public static class TimeAndPage {
        private Date fromDate;
        private Date toDate;
        private Pageable pageable;
    }

    public static <T extends QueryByTimeAndLimitRequest> TimeAndPage todayAndPageDesc(T request) {
        return todayAndPageDesc(request.getFetchCount());
    }

    private static TimeAndPage todayAndPageDesc(Integer fetchCount) {
        Date today = new Date();
        Date fromDate = DateUtils.getStartOfDate(today);
        Date toDate = DateUtils.getEndOfDate(today);
        Pageable pageable = pageDesc(fetchCount);
        TimeAndPage result = new TimeAndPage(fromDate, toDate, pageable);
        return result;
    }

    public static <T extends QueryByTimeAndLimitRequest> TimeAndPage historyAndPageDesc(T request, String action) {
        return historyTimeAccurateToSecondAndPageDesc(request.getFromDate(), request.getToDate(), request.getFetchCount(), action, null);
    }

    private static TimeAndPage historyTimeAccurateToSecondAndPageDesc(String fromDateReq, String toDateReq, Integer fetchCount, String action, String customDefaultFromDate) {
        List<Date> fromDate_toDate = historyTimeAccurateToSecond(fromDateReq, toDateReq, action, customDefaultFromDate);
        Date fromDate = fromDate_toDate.get(0);
        Date toDate = fromDate_toDate.get(1);
        Pageable pageable = pageDesc(fetchCount);
        TimeAndPage result = new TimeAndPage(fromDate, toDate, pageable);
        return result;
    }

    private static List<Date> historyTimeAccurateToSecond(String fromDate, String toDate, String action, String customDefaultFromDate) {
        return historyTime(fromDate, toDate, action, true, customDefaultFromDate);
    }

    private static List<Date> historyTime(String fromDateReq, String toDateReq, String action, Boolean queryWithoutMillis, String customDefaultFromDate) {
        Date fromDate = new Date();
        try {
            fromDate = fromDateReq == null
                    ? DefaultUtils.DATE_FORMAT().parse(customDefaultFromDate != null ? customDefaultFromDate : Constants.DEFAULT_HISTORY_FROM_DATE)
                    : DefaultUtils.DATE_FORMAT().parse(fromDateReq);
            fromDate = DateUtils.getStartOfDate(fromDate);
        } catch (ParseException e) {
            log.warn("Error while {}: parse fromDate failed: {} _ {}", action, fromDateReq, e);
        }
        Date toDate = new Date();
        try {
            Date yesterday = Utils.getYesterday();
            Date toDateByRequest = DefaultUtils.DATE_FORMAT().parse(toDateReq);
            //max toDate is yesterday. today's records are not counted history, they're represented in order book
            toDate = toDateReq == null || toDateByRequest.after(yesterday) ? yesterday : toDateByRequest;
        } catch (ParseException e) {
            log.warn("Error while {}: parse toDate failed: {} _ {}", action, toDateReq, e);
        }
        if (queryWithoutMillis) {
            toDate = DateUtils.getEndOfDate(toDate);
        }
        return Arrays.asList(fromDate, toDate);
    }

    private static Pageable pageDesc(Integer fetchCount) {
        if (fetchCount == null || fetchCount < 0 || fetchCount > Constants.MAX_FETCH_COUNT) {
            fetchCount = Constants.DEFAULT_FETCH_COUNT;
        }
        Sort sortByIdDesc = Sort.by(Sort.Direction.DESC, "id");
        Pageable pageable = PageRequest.of(0, fetchCount, sortByIdDesc);
        return pageable;
    }
}
