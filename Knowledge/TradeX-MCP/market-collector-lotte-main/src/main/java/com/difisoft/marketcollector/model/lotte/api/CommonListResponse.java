package com.difisoft.marketcollector.model.lotte.api;

import lombok.Data;

import java.util.List;

@Data
public class CommonListResponse<I> {
    protected String nextKey;
    protected boolean hasNext;
    protected List<I> list;
}
