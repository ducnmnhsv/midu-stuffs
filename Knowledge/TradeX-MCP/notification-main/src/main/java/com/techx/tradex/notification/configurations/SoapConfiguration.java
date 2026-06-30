package com.techx.tradex.notification.configurations;

import com.techx.tradex.notification.services.SoapClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class SoapConfiguration {
    @Bean
    public Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.techx.tradex.notification.model.wsdl");
        return marshaller;
    }

    @Bean
    public SoapClient oneSmsSoapClient(Jaxb2Marshaller marshaller) {
        SoapClient client = new SoapClient();
        client.setMarshaller(marshaller);
        client.setUnmarshaller(marshaller);
        return client;
    }
}
