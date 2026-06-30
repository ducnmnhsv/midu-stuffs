package com.difisoft.nhsv.admin.service.vietstock;

import com.difisoft.nhsv.admin.config.AppConf;
import com.difisoft.nhsv.admin.config.ApplicationProperties;
import com.difisoft.nhsv.admin.service.vietstock.resquest.VietStockAuthData;
import com.difisoft.nhsv.admin.service.vietstock.resquest.VietStockEventQueryRequest;
import com.difisoft.redis.CoordinatorService;
import com.difisoft.redis.RedisDao;
import com.techx.tradex.common.exceptions.GeneralException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.net.HttpCookie;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class VietStockSupport {
    protected static final String VIETSTOCK_AUTH_DATA_CACHE_KEY = "vietstock_auth_data";
    protected static final String VIETSTOCK_AUTH_DATA_ACQUIRE_LOCK_KEY = "vietstock_auth_data_acquire_lock";
    private final RedisDao redisDao;
    private final AppConf appConf;
    private final CoordinatorService coordinatorService;
    private final ApplicationProperties applicationProperties;


    public VietStockSupport(
        RedisDao redisDao,
        AppConf appConf,
        CoordinatorService coordinatorService,
        ApplicationProperties applicationProperties
    ) {
        this.redisDao = redisDao;
        this.appConf = appConf;
        this.coordinatorService = coordinatorService;
        this.applicationProperties = applicationProperties;
    }

    protected VietStockAuthData getAuthData() {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            String url = appConf.getVietStock().getHost();
            HttpGet request = new HttpGet(url);

            return client.execute(request, httpResponse -> {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(httpResponse.getEntity());

                if (statusCode == HttpStatus.SC_OK) {
                    Document doc = Jsoup.parse(responseBody);
                    String requestToken = Objects.requireNonNull(doc.select("input[name=__RequestVerificationToken]").first()).val();

                    if (StringUtils.isEmpty(requestToken)) {
                        throw new GeneralException("Failed to fetch VietStock request token");
                    }

                    Set<HttpCookie> cookies = Arrays.stream(httpResponse.getHeaders("Set-Cookie"))
                        .flatMap(header -> HttpCookie.parse(header.getValue()).stream())
                        .collect(Collectors.toSet());

                    String cookieString = cookies.stream()
                        .map(cookie -> cookie.getName() + "=" + cookie.getValue())
                        .collect(Collectors.joining("; "));

                    return VietStockAuthData.builder()
                        .cookies(cookieString)
                        .requestToken(requestToken)
                        .build();
                } else {
                    throw new GeneralException("Failed to get auth data from VietStock");
                }
            });
        } catch (Exception e) {
            throw new GeneralException("Failed to get auth data from VietStock");
        }
    }

    public String getEventPage(VietStockEventQueryRequest request) {
        try {
            log.info("Creating HTTP client...");
            CloseableHttpClient client = HttpClients.createDefault();

            String url = appConf.getVietStock().getHost() + appConf.getVietStock().getEvent().getUrl();
            log.info("Constructed VietStock event URL: {}", url);

            HttpPost postRequest = new HttpPost(url);
            appConf.getVietStock().getHeaders().forEach(postRequest::addHeader);
            postRequest.setHeader("Cookie", request.getCookies());

            String body = request.toFormEncodedString();
            log.info("Request headers: {} body: {}", postRequest.getAllHeaders(), body);
            StringEntity entity = new StringEntity(body);
            postRequest.setEntity(entity);

            return client.execute(postRequest, httpResponse -> {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(httpResponse.getEntity());

                log.info("Received response with status code: {}", statusCode);
                log.info("Response body: {}", responseBody);

                if (statusCode == HttpStatus.SC_OK) {
                    return responseBody;
                }

                log.warn("Unexpected status code: {}. Returning null.", statusCode);
                return null;
            });
        } catch (Exception e) {
            log.error("Failed to get event page from VietStock", e);
            return null;
        }
    }

    public Double convertRate(String rateStr) {
        if (StringUtils.isEmpty(rateStr)) {
            return null;
        }

        if (!rateStr.contains(":")) {
            // If the rate string does not contain ":", parse it to double value
            try {
                return Double.parseDouble(rateStr);
            } catch (NumberFormatException e) {
                log.error("Cannot parse rate string: {}", rateStr);
                return null;
            }
        }

        // Split the rate string by ":"
        String[] parts = rateStr.split(":");
        if (parts.length != 2) {
            return null;
        }
        // Parse the rate string from A:B to B/A * 100 (%)
        try {
            double A = Double.parseDouble(parts[0]);
            double B = Double.parseDouble(parts[1]);
            return B / A * 100;
        } catch (NumberFormatException e) {
            log.error("Cannot parse rate string: {}", rateStr);
            return null;
        }
    }

    public VietStockAuthData getAuthDataFromCache() {
        // Get the data from cache first
        VietStockAuthData vietStockAuthData = this.redisDao.get(VIETSTOCK_AUTH_DATA_CACHE_KEY, VietStockAuthData.class);

        // If the data is not null, return it
        if (vietStockAuthData != null) {
            return vietStockAuthData;
        }

        // Otherwise, try to get the data from the vietstock host and save it to cache
        // But first, we need to lock the cache key to prevent multiple nodes from fetching the data at the same time
        String acquireKey = this.coordinatorService.acquire(VIETSTOCK_AUTH_DATA_ACQUIRE_LOCK_KEY, applicationProperties.getNodeId(), 60);
        // If the key is not null, we have acquired the lock
        if (acquireKey != null) {
            log.warn("Acquired the cache key, trying to get the data and save to cache");
            try {
                // Fetch data from host
                vietStockAuthData = this.getAuthData();
                // Save data to cache with 30 minute expiration
                this.redisDao.set(VIETSTOCK_AUTH_DATA_CACHE_KEY, vietStockAuthData, 1800000); // 30*60*1000
                // Release the lock
                this.coordinatorService.release(VIETSTOCK_AUTH_DATA_ACQUIRE_LOCK_KEY);
            } catch (Exception e) {
                log.error("Failed to get VietStock auth data from cache", e);
            }
        } else {
            // If we cannot acquire the lock, wait for the result
            this.coordinatorService.waitForResult(VIETSTOCK_AUTH_DATA_ACQUIRE_LOCK_KEY);
            // Try to get the data from cache again
            vietStockAuthData = this.redisDao.get(VIETSTOCK_AUTH_DATA_CACHE_KEY, VietStockAuthData.class);
        }

        return vietStockAuthData;
    }
}
