package com.techx.tradex.notification.dao;


import com.techx.tradex.notification.model.KakaoMessageRequest;
import com.techx.tradex.notification.model.KakaoMessageResponse;

public interface KakaoDao {
    KakaoMessageResponse sendMessage(KakaoMessageRequest request);
}
