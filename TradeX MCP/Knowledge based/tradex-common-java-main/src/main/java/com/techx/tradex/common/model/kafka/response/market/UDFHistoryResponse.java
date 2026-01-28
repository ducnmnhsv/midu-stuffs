package com.techx.tradex.common.model.kafka.response.market;

import com.techx.tradex.common.constants.UDFHistoryStatusEnum;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UDFHistoryResponse {

    private List<Long> t = new ArrayList<>();
    private List<Integer> o = new ArrayList<>();
    private List<Integer> h = new ArrayList<>();
    private List<Integer> l = new ArrayList<>();
    private List<Integer> c = new ArrayList<>();
    private List<Long> v = new ArrayList<>();
    private String s = UDFHistoryStatusEnum.NO_DATE.getStatus(); // or no_data

}

