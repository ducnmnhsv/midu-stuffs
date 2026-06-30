package com.techx.tradex.notification.services;

import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

@Component
public class SoapClient extends WebServiceGatewaySupport {
    public Object callWebService(String url, Object request, SoapActionCallback soapAction) {
        return getWebServiceTemplate().marshalSendAndReceive(url, request, soapAction);
    }
}
