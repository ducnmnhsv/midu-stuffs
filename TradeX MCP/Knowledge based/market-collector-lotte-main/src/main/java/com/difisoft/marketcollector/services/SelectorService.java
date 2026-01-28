package com.difisoft.marketcollector.services;

import com.difisoft.htsconnection.socket.nonblocking.SelectorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.Selector;

@Component
public class SelectorService {
    private static final Logger log = LoggerFactory.getLogger(SelectorService.class);
    private SelectorHandler selectorHandler;

    public SelectorService() {
        try {
            selectorHandler = new SelectorHandler();
            new Thread(selectorHandler).start();
        } catch (IOException e) {
            log.error("Error creating selector handler", e);
        }
    }

    public Selector get() {
        return selectorHandler.getSelector();
    }
}
