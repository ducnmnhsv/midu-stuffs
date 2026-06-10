package com.techx.tradex.ekycadmin.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techx.tradex.common.kafka.KafkaRequestHandler;
import com.techx.tradex.common.model.kafka.Message;
import com.techx.tradex.ekycadmin.config.AppConf;
import com.techx.tradex.ekycadmin.models.request.*;
import com.techx.tradex.ekycadmin.models.request.EKycAddReq;
import com.techx.tradex.ekycadmin.models.request.EKycStatusReq;
import com.techx.tradex.ekycadmin.models.request.FptECSignRequest;
import com.techx.tradex.ekycadmin.models.request.SendOtpRequest;
import com.techx.tradex.ekycadmin.models.request.VerifyOtpRequest;
import com.techx.tradex.ekycadmin.service.CustomEKycService;
import com.techx.tradex.ekycadmin.service.EContractCustomService;
import com.techx.tradex.ekycadmin.service.EKycStatusService;
import com.techx.tradex.ekycadmin.service.LotteEKycService;
import com.techx.tradex.ekycadmin.service.OTPService;
import com.techx.tradex.ekycadmin.utils.CommonUtil;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

@Service
public class RequestHandler extends KafkaRequestHandler {

    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
    private final AppConf appConf;
    private final ObjectMapper objectMapper;
    private final CustomEKycService customEKycService;
    private final OTPService otpService;
    private final EKycStatusService eKycStatusService;
    private final LotteEKycService lotteEKycService;
    private final EContractCustomService eContractCustomService;

    @Autowired
    public RequestHandler(
            ObjectMapper objectMapper,
            AppConf appConf,
            CustomEKycService customEKycService,
            OTPService otpService,
            EKycStatusService eKycStatusService,
            EContractCustomService eContractCustomService,
            LotteEKycService lotteEKycService) {
        super(objectMapper, appConf.getKafkaBootstraps(), appConf.getClusterId(), 3);
        this.appConf = appConf;
        this.objectMapper = objectMapper;
        this.customEKycService = customEKycService;
        this.otpService = otpService;
        this.eKycStatusService = eKycStatusService;
        this.lotteEKycService = lotteEKycService;
        this.eContractCustomService = eContractCustomService;
    }

    @Override
    protected Object handle(Message message) {
        if (message == null || message.getData() == null) {
            log.error("Invalid data");
            return true;
        }
        try {
            log.info("message: {}", message);
            if (message.getUri().equals("/api/v1/ekyc-admin/ekyc/add") || message.getUri().equals("/api/v1/ekycs")) {
                EKycAddReq request = Message.getData(objectMapper, message, EKycAddReq.class);
                log.debug("request in object {}, request in json {}, token in json {}, refreshTokenId: {}", request, CommonUtil.objectToStringJsonIgnoreError(request), CommonUtil.objectToStringJsonIgnoreError(request.getHeaders().getToken()), request.getHeaders().getToken().getRefreshTokenId());
                return customEKycService.addEKyc(message.getTransactionId(), request);
            }
            if (message.getUri().equals("/api/v1/ekyc-admin/sendOtp")) {
                SendOtpRequest request = Message.getData(objectMapper, message, SendOtpRequest.class);
                return this.otpService.generateAndSendOtp(request);
            }
            if (message.getUri().equals("/api/v1/ekyc-admin/verifyOtp")) {
                VerifyOtpRequest request = Message.getData(objectMapper, message, VerifyOtpRequest.class);
                return this.otpService.VerifyOtp(request);
            }
            if (message.getUri().equals("/api/v1/ekyc-admin/statuses")) {
                EKycStatusReq request = Message.getData(objectMapper, message, EKycStatusReq.class);
                return this.eKycStatusService.getEKycStatus(request);
            }
            if (message.getUri().equals("/api/v1/ekycs/create")) {
                EKycAddReq request = Message.getData(objectMapper, message, EKycAddReq.class);
                return lotteEKycService.createEKycLotte(message.getTransactionId(), request);
            }
            if (message.getUri().equals("/api/v1/ekyc-admin/eContractStatus")) {
                EContractStatusReq request = Message.getData(objectMapper, message, EContractStatusReq.class);
                return eContractCustomService.getEContractStatus(request);
            }
            if (message.getUri().equals("/api/v1/equity/account/contracts")) {
                FptECSignRequest request = Message.getData(objectMapper, message, FptECSignRequest.class);
                return this.eContractCustomService.signEContract(request);
            }
            if (message.getUri().equals("internal:/api/v1/ekycs/get")) {
                InternalGetEKycRequest request = Message.getData(objectMapper, message, InternalGetEKycRequest.class);
                return this.customEKycService.internalGetEkyc(request);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
            return Observable.error(e);
        }
        return true;
    }

    public void sendBroadcastMessageSafe(String uri, Object data) {
        try {
            this.sendBroadcastMessage(uri, data);
        } catch (Exception e) {
            log.error("fail to send broadcast message to {} with data {}", uri, data, e);
        }
    }

    public void sendBroadcastMessage(String uri, Object data) throws IOException {
        this.sendMessage(BroadcastHandler.getBroadcastTopic(this.appConf), uri, data, null);
    }
}
