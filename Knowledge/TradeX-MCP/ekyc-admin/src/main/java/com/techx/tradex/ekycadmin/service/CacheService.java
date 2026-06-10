package com.techx.tradex.ekycadmin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techx.tradex.ekycadmin.dao.RedisDao;
import com.techx.tradex.ekycadmin.models.redis.Otp;
import com.techx.tradex.ekycadmin.models.redis.OtpValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CacheService {

    private RedisDao redisDao;

    @Autowired
    public CacheService(RedisDao redisDao) {
        this.redisDao = redisDao;
    }

    void addOtp(Otp otp, String verificationId, Long otpTtl) throws JsonProcessingException {
        this.redisDao.addOtp(otp, verificationId, otpTtl);
    }

    void addOtpKey(Otp otp, String verificationId, Long otpTtl) throws JsonProcessingException {
        this.redisDao.addOtpKey(otp, verificationId, otpTtl);
    }

    void addOtpValidation(OtpValidation otp) throws JsonProcessingException {
        this.redisDao.addOtpValidation(otp);
    }

    Otp findOtp(String verificationId) throws JsonProcessingException {
        return this.redisDao.findOtp(verificationId);
    }

    Otp findOtpKey(String verificationId) throws JsonProcessingException {
        return this.redisDao.findOtpKey(verificationId);
    }

    OtpValidation findOtpValidation(String username) throws JsonProcessingException {
        return this.redisDao.findOtpValidation(username);
    }

    void removeVerifiedOtp(String id) {
        this.redisDao.removeVerifiedOtp(id);
    }

    void removeVerifiedOtpKey(String id) {
        this.redisDao.removeVerifiedOtpKey(id);
    }
}
