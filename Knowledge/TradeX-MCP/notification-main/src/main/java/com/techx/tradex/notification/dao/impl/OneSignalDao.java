package com.techx.tradex.notification.dao.impl;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.techx.tradex.notification.model.SmsOneSignalRequest;

@Repository
public class OneSignalDao implements com.techx.tradex.notification.dao.OneSignalDao{
    private static final Logger logger = LoggerFactory.getLogger(OneSignalDao.class);
    private static final String ENCODING = "UTF-8";
    public void sendRequestOneSignal(SmsOneSignalRequest request) {
        try {
            String jsonResponse;
            String basicAuth = "Basic " + request.getAppKey();

            URL url = new URL(request.getMethod());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setUseCaches(false);
            con.setDoOutput(true);
            con.setDoInput(true);

            con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", basicAuth);
            
            logger.info("Filter when request to OneSignal: {}", request.getFilter());
            byte[] sendBytes = request.getFilter().getBytes(ENCODING);
            con.setFixedLengthStreamingMode(sendBytes.length);
            
            OutputStream outputStream = con.getOutputStream();
            outputStream.write(sendBytes);

            int httpResponse = con.getResponseCode();
            logger.info("HttpResponse {}", httpResponse);
            if (httpResponse == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(con.getInputStream(), ENCODING);
                jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                scanner.close();
                logger.info("JsonResponse {}", jsonResponse);
            } else {
                Scanner scanner = new Scanner(con.getErrorStream(), ENCODING);
                jsonResponse = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                scanner.close();
                logger.error("JsonResponse {}", jsonResponse);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
