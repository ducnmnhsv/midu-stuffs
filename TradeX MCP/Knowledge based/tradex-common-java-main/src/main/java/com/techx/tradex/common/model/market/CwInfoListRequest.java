package com.techx.tradex.common.model.market;

import lombok.Data;

import java.util.List;

@Data
public class CwInfoListRequest {
    List<String> cwList;
    String cwCode;
    Integer fetchCount;
}
