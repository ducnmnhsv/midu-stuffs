package com.techx.tradex.ekycadmin.service.impl;

import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.constant.Constants;
import com.techx.tradex.ekycadmin.constant.Messages;
import com.techx.tradex.ekycadmin.service.CommonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonServiceImpl implements CommonService {

    private final AppConf appConf;

    @Override
    public void validateTraDexTokenGrantType(String grantType) throws GeneralException {
        if (!(
                StringUtils.equalsIgnoreCase(grantType, Constants.EContract.CLIENT_CREDENTIALS)
                        || StringUtils.equalsIgnoreCase(grantType, Constants.EContract.PASSWORD)
                        || StringUtils.equalsIgnoreCase(grantType, Constants.EContract.PASSWORD_OTP)
                        || StringUtils.equalsIgnoreCase(grantType, Constants.EContract.BIOMETRIC)
                        || StringUtils.equalsIgnoreCase(grantType, Constants.EContract.BIOMETRIC_OTP)
        )) {
            throw new GeneralException(MessageFormat.format(Messages.GRANT_TYPE_IS_INVALID, grantType));
        }
    }

    @Override
    public int getChunk(int size) {
        return size < 200
            ? 10
            : size < 400 ? 20 : size < 800 ? 30 : size < 2000 ? 60 : size < 5000 ? 150 : appConf.getThreadPool().getMaxPoolSize();
    }
}
