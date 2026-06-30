package com.techx.tradex.notification.dao.impl;

import com.google.gson.JsonParser;
import com.techx.tradex.common.model.notification.NotificationMessage;
import com.techx.tradex.notification.configurations.AppConf;
import com.vng.zalo.sdk.APIException;
import com.vng.zalo.sdk.oa.ZaloOaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.gson.JsonObject;
import java.util.HashMap;
import java.util.Map;

@Repository
public class ZaloDao implements com.techx.tradex.notification.dao.ZaloDao {

    @Autowired
    private AppConf appConf;
    private ZaloOaClient client = new ZaloOaClient();
    private static final Logger log = LoggerFactory.getLogger(EmailDao.class);

    @Override
    public void sendMessage(NotificationMessage notificationMessage, String toList) throws Exception {
        if (toList == null || toList.equals("")) {
            return;
        }
        int retry = 0;
        sendMessageById(notificationMessage, toList, retry);
    }


    @Override
    public void sendMessageById(NotificationMessage notificationMessage, String userId, int retry) throws APIException {
        Map<String, Object> param = new HashMap<>();
        param.put("access_token", appConf.getZalo().getAccessToken());
        JsonObject id = new JsonObject();
        id.addProperty("user_id", userId);
        JsonObject text = new JsonObject();
        String msgType = (String) notificationMessage.getTemplate().keySet().toArray()[0];
        Map<String, String> msg = (Map<String, String>) notificationMessage.getTemplate().get(msgType);
        text.addProperty("text", msg.get("message"));
        JsonObject body = new JsonObject();
        body.add("recipient", id);
        body.add("message", text);
        JsonObject res = client.excuteRequest(appConf.getZalo().getSendMessageUrl(), "POST", param, body);
        JsonParser parser = new JsonParser();
        JsonObject response = (JsonObject) parser.parse(res.toString());
        if (!response.get("error").toString().equals("0")) {
            if (retry == 3) {
                return;
            }
            retry++;
            log.info("Error send message with status: {} to user {}", res.toString(), userId);
            sendMessageById(notificationMessage, userId, retry);
        } else {
            log.info("Success send message with status {}", res.toString());
        }
    }
}
