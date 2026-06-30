package com.difisoft.nhsv.admin.service;

import com.difisoft.kafka.handler.RequestContext;
import com.difisoft.model.exceptions.GeneralException;
import com.difisoft.model.exceptions.SubErrorsException;
import com.difisoft.model.responses.MessageResponse;
import com.difisoft.nhsv.admin.constant.Constants;
import com.difisoft.nhsv.admin.domain.request.FeedbackRequest;
import com.difisoft.nhsv.admin.repository.FeedbackRepository;
import com.techx.tradex.common.constants.ErrorCodeEnums;

import java.util.Collections;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FeedbackService {

    private final Logger log = LoggerFactory.getLogger(FeedbackService.class);

    private final MailService mailService;

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(MailService mailService, FeedbackRepository feedbackRepository) {
        this.mailService = mailService;
        this.feedbackRepository = feedbackRepository;
    }

    public MessageResponse saveFeedback(FeedbackRequest request,
            RequestContext<FeedbackRequest> ctx) {
        log.info("FeedbackService.saveFeedback");
        if (Objects.isNull(request.getEmail()) || request.getEmail().isEmpty()) {
            throw new SubErrorsException(ErrorCodeEnums.INVALID_PARAMETER.name())
                    .add(Constants.FIELD_IS_REQUIRED, "email", Collections.singletonList("email"));
        }
        if (Objects.isNull(request.getFullName()) || request.getFullName().isEmpty()) {
            throw new SubErrorsException(ErrorCodeEnums.INVALID_PARAMETER.name())
                    .add(Constants.FIELD_IS_REQUIRED, "fullName", Collections.singletonList("fullName"));
        }
        if (Objects.isNull(request.getPhoneNo()) || request.getPhoneNo().isEmpty()) {
            throw new SubErrorsException(ErrorCodeEnums.INVALID_PARAMETER.name())
                    .add(Constants.FIELD_IS_REQUIRED, "phoneNo", Collections.singletonList("phoneNo"));
        }
        if (Objects.isNull(request.getMessage()) || request.getMessage().isEmpty()) {
            throw new SubErrorsException(ErrorCodeEnums.INVALID_PARAMETER.name())
                    .add(Constants.FIELD_IS_REQUIRED, "message", Collections.singletonList("message"));
        }
        if(request.getImageUrl().size() > 5){
            throw new GeneralException(Constants.MAX_5_IMAGES);
        }
        mailService.sendFeedbackEmail(request);
        feedbackRepository.save(request.toFeedback(request));
        return new MessageResponse(Constants.FEEDBACK_SENT);
    }
}
