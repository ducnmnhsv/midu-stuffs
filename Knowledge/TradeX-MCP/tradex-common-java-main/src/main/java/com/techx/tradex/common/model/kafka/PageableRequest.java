package com.techx.tradex.common.model.kafka;

import lombok.Data;

import java.util.List;

@Data
public class PageableRequest {
    private int size = 20;
    private int pageNo = 0;
    private List<Order> orders;

    @Data
    public static class Order {
        private String field;
        private boolean asc;
    }
}
