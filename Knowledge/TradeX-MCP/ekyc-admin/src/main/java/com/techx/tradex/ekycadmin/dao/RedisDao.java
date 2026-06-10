package com.techx.tradex.ekycadmin.dao;

import static com.techx.tradex.ekycadmin.utils.Util.toDateFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.constants.RedisDataTypeEnum;
import com.techx.tradex.common.exceptions.GeneralException;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.constant.Constants;
import com.techx.tradex.ekycadmin.models.enums.OtpIdType;
import com.techx.tradex.ekycadmin.models.redis.Otp;
import com.techx.tradex.ekycadmin.models.redis.OtpValidation;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisDao {

    private static final Logger log = LoggerFactory.getLogger(RedisDao.class);

    private RedisTemplate<String, String> redisTemplate;
    private ObjectMapper objectMapper;
    private AppConf appConf;

    @Autowired
    public RedisDao(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper, AppConf appConf) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.appConf = appConf;
    }

    public void addOtp(Otp otp, String otpId, Long otpLifeTime) throws JsonProcessingException {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        otpLifeTime =
            otpLifeTime == null
                ? (
                    otp.getOtpIdType().equals(OtpIdType.EMAIL.name())
                        ? appConf.getOtpLifeTime().getEmail().get("DEFAULT_EXPIRE_TIME")
                        : appConf.getOtpLifeTime().getSms().get("DEFAULT_EXPIRE_TIME")
                )
                : otpLifeTime;
        valueOperations.set(Constants.KIS_E_KYC_OTP_ + otpId, objectMapper.writeValueAsString(otp), otpLifeTime, TimeUnit.SECONDS);
    }

    public void addOtpKey(Otp otp, String otpId, Long otpLifeTime) throws JsonProcessingException {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(Constants.KIS_E_KYC_OTP_KEY_ + otpId, objectMapper.writeValueAsString(otp), otpLifeTime, TimeUnit.SECONDS);
    }

    public void addOtpValidation(OtpValidation otp) throws JsonProcessingException {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String otpId = otp.getUsername() + "_" + toDateFormat(LocalDateTime.now());
        valueOperations.set(Constants.KIS_E_KYC_OTP_ + otpId, objectMapper.writeValueAsString(otp), 1, TimeUnit.DAYS);
    }

    public OtpValidation findOtpValidation(String username) throws JsonProcessingException {
        ValueOperations<String, String> setOperations = redisTemplate.opsForValue();
        String verificationStr = setOperations.get(Constants.KIS_E_KYC_OTP_ + username + "_" + toDateFormat(LocalDateTime.now()));
        if (verificationStr != null) {
            return this.objectMapper.readValue(verificationStr, OtpValidation.class);
        } else {
            throw new GeneralException(Constants.OBJECT_NOT_FOUND);
        }
    }

    public Otp findOtp(String verificationId) throws JsonProcessingException {
        ValueOperations<String, String> setOperations = redisTemplate.opsForValue();
        String verificationStr = setOperations.get(Constants.KIS_E_KYC_OTP_ + verificationId);
        if (verificationStr != null) {
            return this.objectMapper.readValue(verificationStr, Otp.class);
        } else {
            throw new GeneralException(Constants.OBJECT_NOT_FOUND);
        }
    }

    public Otp findOtpKey(String verificationId) throws JsonProcessingException {
        ValueOperations<String, String> setOperations = redisTemplate.opsForValue();
        String verificationStr = setOperations.get(Constants.KIS_E_KYC_OTP_KEY_ + verificationId);
        if (verificationStr != null) {
            return this.objectMapper.readValue(verificationStr, Otp.class);
        } else {
            throw new GeneralException(Constants.OBJECT_NOT_FOUND);
        }
    }

    public void removeVerifiedOtp(String id) {
        ValueOperations<String, String> setOperations = redisTemplate.opsForValue();
        setOperations.set(Constants.KIS_E_KYC_OTP_ + id, "", 1, TimeUnit.MILLISECONDS);
    }

    public void removeVerifiedOtpKey(String id) {
        ValueOperations<String, String> setOperations = redisTemplate.opsForValue();
        setOperations.set(Constants.KIS_E_KYC_OTP_KEY_ + id, "", 1, TimeUnit.MILLISECONDS);
    }

    public <T> void set(String key, T value, long ttlInMs) throws JsonProcessingException {
        redisTemplate.opsForValue().set(key, objectToString(value), ttlInMs, TimeUnit.MILLISECONDS);
    }

    public boolean isExists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private <E> String objectToString(E data) throws JsonProcessingException {
        if (data == null) {
            return RedisDataTypeEnum.NULL.getType();
        } else if (data instanceof Boolean) {
            return RedisDataTypeEnum.BOOLEAN.getType() + data;
        } else if (data instanceof String) {
            return RedisDataTypeEnum.STRING.getType() + data;
        } else {
            return data instanceof Number
                ? RedisDataTypeEnum.NUMBER.getType() + data
                : RedisDataTypeEnum.OBJECT.getType() + this.objectMapper.writeValueAsString(data);
        }
    }
}
