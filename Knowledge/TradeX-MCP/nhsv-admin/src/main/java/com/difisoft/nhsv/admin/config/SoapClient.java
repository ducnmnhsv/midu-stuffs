package com.difisoft.nhsv.admin.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

@Component
public class SoapClient extends WebServiceGatewaySupport {
    @Autowired
    public SoapClient(WebServiceTemplate webServiceTemplate) {
        this.setWebServiceTemplate(webServiceTemplate);
    }

    public Object callWebService(String url, Object request, SoapActionCallback soapAction) {
        return getWebServiceTemplate().marshalSendAndReceive(url, request, soapAction);
    }
}
