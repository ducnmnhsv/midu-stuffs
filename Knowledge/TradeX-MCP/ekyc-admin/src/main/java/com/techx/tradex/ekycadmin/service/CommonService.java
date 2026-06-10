package com.techx.tradex.ekycadmin.service;

import com.techx.tradex.common.exceptions.GeneralException;

public interface CommonService {
    void validateTraDexTokenGrantType(String grantType) throws GeneralException;
    int getChunk(int size);
}
