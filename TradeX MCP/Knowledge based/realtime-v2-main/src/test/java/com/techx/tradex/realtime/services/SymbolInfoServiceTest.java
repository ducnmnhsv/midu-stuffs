package com.techx.tradex.realtime.services;

import com.difisoft.kafka.handler.RequestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class SymbolInfoServiceTest {

    @Autowired
    private SymbolInfoService symbolInfoService;

    @Test
    public void test_stockTopWorstReturnsInfoExecute() {
        symbolInfoService.stockTopWorstReturnsInfoExecute(null, new RequestContext<>("test_stockTopWorstReturnsInfoExecute", null));
        assert true;
    }

}
