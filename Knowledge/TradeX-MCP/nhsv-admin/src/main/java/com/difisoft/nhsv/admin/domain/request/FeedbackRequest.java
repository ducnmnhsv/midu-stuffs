package com.difisoft.nhsv.admin.domain.request;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import com.difisoft.nhsv.admin.domain.Feedback;

import lombok.Data;

@Data
public class FeedbackRequest {
    private String email;
    private String fullName;
    private String phoneNo;
    private String message;
    private List<String> imageUrl;


    public Feedback toFeedback(FeedbackRequest request) {
        Feedback feedback = new Feedback();
        feedback.setEmail(request.getEmail());
        feedback.setFullName(request.getFullName());
        feedback.setPhoneNo(request.getPhoneNo());
        feedback.setMessage(request.getMessage());
        feedback.setImageUrl(request.getImageUrl() != null ? String.join("||", request.getImageUrl()) : null);
        feedback.setCreatedAt(ZonedDateTime.now());
        return feedback;
    }
}
