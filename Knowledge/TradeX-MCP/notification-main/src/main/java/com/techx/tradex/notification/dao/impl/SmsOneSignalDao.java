package com.techx.tradex.notification.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.techx.tradex.common.exceptions.InvalidFormatException;
import com.techx.tradex.notification.controllers.RequestHandler;
import com.techx.tradex.notification.model.PhoneNumberRegisterFilter;
import com.techx.tradex.notification.model.SmsOneSignalRequest;
import com.techx.tradex.notification.model.SmsSendlFilter;

@Repository
public class SmsOneSignalDao implements com.techx.tradex.notification.dao.SmsOneSignalDao {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    private static final String SIGNUP = "https://onesignal.com/api/v1/players";
    private static final String SEND_NOTIFICATION = "https://onesignal.com/api/v1/notifications";
    private static final String DEVICE_TYPE = "14";
    private OneSignalDao oneSignalDao;
    private ObjectMapper objectMapper;
    
    @Autowired
    public SmsOneSignalDao(OneSignalDao oneSignalDao, ObjectMapper objectMapper) {
        this.oneSignalDao = oneSignalDao;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendSms(SmsOneSignalRequest request) throws Exception {
        List<String> listPhoneTo = new ArrayList<String>();
        listPhoneTo.add(request.getPhoneTo());
        Map<String, String> mapContents = new HashMap<String, String>();
        mapContents.put("en", request.getContent());
        SmsSendlFilter smsSendlFilter = new SmsSendlFilter();
        smsSendlFilter.setAppId(request.getAppID());
        smsSendlFilter.setSmsFrom(request.getPhoneFrom());
        smsSendlFilter.setContents(mapContents);
        smsSendlFilter.setPhoneNumbers(listPhoneTo);
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String filterSendSMS = ow.writeValueAsString(smsSendlFilter);
        request.setFilter(filterSendSMS);
        request.setMethod(SEND_NOTIFICATION);
        oneSignalDao.sendRequestOneSignal(request);
    }

    @Override
    public void registerPhoneNumber(SmsOneSignalRequest request)  {
      PhoneNumberRegisterFilter phoneNumberRegisterFilter = new PhoneNumberRegisterFilter();
      phoneNumberRegisterFilter.setAppId(request.getAppID());
      phoneNumberRegisterFilter.setDeviceType(DEVICE_TYPE);
      phoneNumberRegisterFilter.setIdentifier(request.getPhoneTo());
      phoneNumberRegisterFilter.setUserId(request.getPhoneTo());
      ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
      try {
        String filterRegisterPhoneNumber = ow.writeValueAsString(phoneNumberRegisterFilter);
        request.setFilter(filterRegisterPhoneNumber);
      } catch (JsonProcessingException e) {
          logger.error("Error: {}",e);
          throw new InvalidFormatException("FilterRegisterPhoneNumber");
      }
      request.setMethod(SIGNUP);
      oneSignalDao.sendRequestOneSignal(request);
    }

    

}

