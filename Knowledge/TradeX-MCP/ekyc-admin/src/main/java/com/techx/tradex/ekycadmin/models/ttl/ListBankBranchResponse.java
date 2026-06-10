package com.techx.tradex.ekycadmin.models.ttl;

import lombok.Data;

import java.util.List;

@Data
public class ListBankBranchResponse extends TTLRes {
    private Integer totalCount;
    private String mvResult;
    private List<Item> mainResult;

    @Data
    public static class Item {
        private String bankID;
        private String bankBranch;
        private String bankBranchDesc;
    }
}
