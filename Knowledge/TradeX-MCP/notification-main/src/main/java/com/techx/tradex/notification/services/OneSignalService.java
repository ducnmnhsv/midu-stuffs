package com.techx.tradex.notification.services;

import com.currencyfair.onesignal.OneSignal;
import com.currencyfair.onesignal.model.notification.CreateNotificationResponse;
import com.currencyfair.onesignal.model.notification.Filter;
import com.currencyfair.onesignal.model.notification.Relation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.common.exceptions.InvalidValueException;
import com.techx.tradex.common.model.notification.NotificationMessage;
import com.techx.tradex.common.model.notification.OneSignalConfiguration;
import com.techx.tradex.common.utils.LambdaUtils;
import com.techx.tradex.notification.configurations.AppConf;
import com.techx.tradex.notification.controllers.ResponseProcess;
import com.techx.tradex.notification.dao.TemplateDao;
import com.techx.tradex.notification.model.NotificationDataRequest;
import com.techx.tradex.notification.model.UpdateNotificationRequest;
import com.techx.tradex.notification.model.dto.NotificationDTO;
import com.techx.tradex.notification.util.CommonUtil;
import joptsimple.internal.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


@Service
public class OneSignalService {
    private static final String[] LANGUAGES = new String[]{"en", "vi", "ko"};
    private static final String TITLE_BEGIN_TAG = "<OneSignalTitle>";
    private static final String TITLE_END_TAG = "</OneSignalTitle>";
    private static final Logger log = LoggerFactory.getLogger(OneSignalService.class);
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TemplateDao templateDao;

    @Autowired
    private AppConf appConf;

    @Autowired
    private RequestSender requestSender;

    @Autowired
    private RetryTemplate retryTemplate;

    private static final String ERROR_NOT_SUBSCRIBED = "All included players are not subscribed";

    @Async
    @Transactional
    public void sendNotification(NotificationMessage notificationMessage, ResponseProcess<Object> responseProcess) {
        AtomicBoolean sendNotification = new AtomicBoolean(false);
        Set<String> keys = notificationMessage.getTemplate().keySet();
        AtomicReference<String> key = new AtomicReference<>(Strings.EMPTY);
        AtomicReference<Long> userId = new AtomicReference<>(-1L);
        try {
            log.info("notificationMessage: {}", CommonUtil.objectToStringJsonIgnoreError(notificationMessage));
            if (notificationMessage.getTemplate() == null || notificationMessage.getTemplate().isEmpty()) {
                throw new InvalidValueException("template");
            }
            AppConf.OneSignalApp oneSignalApp = appConf.getOneSignalMap().getOrDefault(notificationMessage.getDomain(), null);
            log.info("oneSignalApp: {}", CommonUtil.objectToStringJsonIgnoreError(oneSignalApp));
            if (oneSignalApp == null) {
                throw new InvalidValueException("domain");
            }
            Map<String, String> templatesMap = appConf.getTemplatesMap().get(notificationMessage.getDomain());
            OneSignalConfiguration request = notificationMessage.getConfiguration(objectMapper, OneSignalConfiguration.class);
            log.info("request: {}", CommonUtil.objectToStringJsonIgnoreError(request));
            request.setAppId(oneSignalApp.getAppId());
            List<NotificationDTO> notificationStoreList = new ArrayList<>();
            notificationMessage.getTemplate().forEach(LambdaUtils.throwBiConsumer((template, templateData) -> {
                Map<String, String> contents = new HashMap<>();
                Map<String, String> headings = new HashMap<>();
                for (String lang : LANGUAGES) {
                    String content = templateDao.getTemplate(template + ".ftl", lang, templateData);
                    if (content != null) {
                        content = content.trim();
                        if (content.startsWith(TITLE_BEGIN_TAG)) {
                            int index = content.indexOf(TITLE_END_TAG);
                            if (index > 0) {
                                String title = content.substring(TITLE_BEGIN_TAG.length(), index);
                                headings.put(lang, title);
                                content = content.substring(index + TITLE_END_TAG.length());
                            }
                        }
                        contents.put(lang, content);
                    }
                }
                request.setTemplateId(templatesMap == null ? "" : templatesMap.getOrDefault(template, ""));
                request.setContents(contents);
                if (!headings.isEmpty()) {
                    request.setHeadings(headings);
                }
                log.info("one signal request {}", request.getContents());
                Map<String, String> data = new HashMap<>();
                objectMapper.convertValue(templateData, Map.class)
                        .forEach((k, v) -> data.put(k.toString(), v == null ? null : v.toString()));
                request.setData(data);
                if (notificationMessage.getType() != null && notificationMessage.getType().equals("KIS_MATCHED_ORDER_SUCCESS")) {
                    if (data.get("status").equals("FILLED_ALL") || data.get("status").equals("PARTIALLY_FILL")) {
                        sendNotification.set(true);
                    }
                }
                if (notificationMessage.getUrl() != null && !notificationMessage.getUrl().isEmpty()) {
                    request.setUrl(notificationMessage.getUrl());
                }
                try {
                    CreateNotificationResponse finalRes = retryTemplate.execute(context -> {
                        log.info("request :{}", CommonUtil.objectToStringJsonIgnoreError(request));
                        CreateNotificationResponse res = OneSignal.createNotification(oneSignalApp.getApiKey(), request);
                        log.info("onesignal response: {}", res);
                        if (res.getErrors() != null
                                && res.getErrors().getClass() == ArrayList.class
                                && ((ArrayList<String>) res.getErrors()).contains(ERROR_NOT_SUBSCRIBED)) {
                            throw new GeneralException(res.getErrors().toString());
                        }
                        return res;
                    });
                    log.info("onesignal response after retry: {}", finalRes);
                } catch (Exception e) {
                    log.error("onesignal response error after retry: ", e);
                }
                String keyRaw = data.get("key");
                String userIdRaw = data.get("userId");
                if (Objects.nonNull(keyRaw) && Objects.nonNull(userIdRaw)) {
                    key.set(keyRaw);
                    userId.set(Long.valueOf(userIdRaw));
                    setNotificationData(keys, userId.get(), request.getContents(), notificationStoreList);
                }
            }));
            List<Filter> filters = request.getFilters();
            if (filters != null && !filters.isEmpty()) {
                for (Filter filter : filters) {
                    if (filter.getKey().equals("userid") && filter.getRelation().equals(Relation.NOT_EXISTS)) {
                        log.info("Not save Notification to DB");
                        return;
                    }
                }
                if (filters.size() == 1 && filters.get(0).getKey().equals("partner_kis")
                        && sendNotification.get() == true) {
                    UpdateNotificationRequest req = new UpdateNotificationRequest();
                    req.setContent(request.getContents().get("en"));
                    req.setContentVi(request.getContents().get("vi"));
                    req.setType(notificationMessage.getType());
                    req.setKisUserName(filters.get(0).getValue());
                    req.setTitle("Matching Order");
                    req.setTitleVi("Chi Tiết Khớp Lệnh");
                    req.setDate(new Date());
                    req.setIsRead(false);
                    requestSender.get().sendMessageNoResponse(appConf.getNotification(), appConf.getNotificationUri(),
                            req);
                }
                filters.removeIf(filter -> filter.getKey().equals("partner_kis") || filter.getKey().equals("userid"));
                if (filters.size() == 1) {
                    UpdateNotificationRequest req = new UpdateNotificationRequest();
                    req.setContent(request.getContents().get("en"));
                    req.setContentVi(request.getContents().get("vi"));
                    req.setType(filters.get(0).getKey());
                    if (request.getHeadings() != null && !request.getHeadings().isEmpty()) {
                        req.setTitle(request.getHeadings().get("en"));
                        req.setTitleVi(request.getHeadings().get("vi"));
                    }
                    req.setDate(new Date());
                    requestSender.get().sendMessageNoResponse(appConf.getNotification(), appConf.getNotificationUri(),
                            req);
                }
            }

            // send notification store ceiling/floor
            if (!Objects.equals(userId.get(), -1L)) {
                log.info("notificationStoreList: {}", CommonUtil.objectToStringJsonIgnoreError(notificationStoreList));
                requestSender.get().sendMessage(
                        appConf.getTopics().getPaaveNotification()
                        , "internal:/api/v1/notification/storeNotification"
                        , new NotificationDataRequest(key.get(), userId.get(), notificationStoreList)
                );
            }
        } catch (Exception e) {
            log.error("[sendNotification] error: ", e);
            if (responseProcess != null) {
                responseProcess.response(null, notificationMessage, e);
            }
        }
    }

    public void setNotificationData(
            Set<String> template
            , Long userId
            , Map<String, String> contents
            , List<NotificationDTO> list
    ) {
        if (template.contains("paave_stock_ceiling_floor_status")) {
            log.info("[saveNotification] template: {}, userId: {}, contents: {}", template, userId, CommonUtil.objectToStringJsonIgnoreError(contents));
            NotificationDTO entity = NotificationDTO.builder()
                    .userId(userId)
                    .date(new Date())
                    .title("Your Portfolio Alert")
                    .titleVi("Chú Ý Danh Mục")
                    .content(contents.get("en"))
                    .contentVi(contents.get("vi"))
                    .type("CEILING_FLOOR_ALERT")
                    .isRead(false)
                    .subAccount("000")
                    .build();
            list.add(entity);
        }
    }
}
