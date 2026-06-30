package com.techx.tradex.notification.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.techx.tradex.notification.model.EmailSendFilter;
import com.techx.tradex.notification.model.SmsOneSignalRequest;

@Repository
public class EmailOneSignalDao implements com.techx.tradex.notification.dao.EmailOneSignalDao{
    private static final String SEND_NOTIFICATION = "https://onesignal.com/api/v1/notifications";
    private OneSignalDao oneSignalDao;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    public EmailOneSignalDao(OneSignalDao oneSignalDao) {
        this.oneSignalDao = oneSignalDao;
    }
    
    @Override
    public void sendEmail(SmsOneSignalRequest request) throws Exception {
        List<String> emailTo = new ArrayList<String>();
        emailTo.add(request.getEmailTo());
        EmailSendFilter emailOneSignalFilter = new EmailSendFilter();
        emailOneSignalFilter.setAppId(request.getAppID());
        emailOneSignalFilter.setTo(emailTo);
        emailOneSignalFilter.setSubject(request.getSubject());
        emailOneSignalFilter.setBody(request.getOtp());
        emailOneSignalFilter.setFromName(request.getEmailFromName());
        ObjectWriter ow = objectMapper.writer().withDefaultPrettyPrinter();
        String filterSendEmail = ow.writeValueAsString(emailOneSignalFilter);
        request.setFilter(filterSendEmail);
        request.setMethod(SEND_NOTIFICATION);
        oneSignalDao.sendRequestOneSignal(request);
    }
}
