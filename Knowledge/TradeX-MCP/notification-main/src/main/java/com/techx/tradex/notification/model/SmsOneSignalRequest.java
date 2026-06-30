package com.techx.tradex.notification.model;

import lombok.Data;

@Data
public class SmsOneSignalRequest {
	private String appID;
	private String appKey;
	private String method;
	private String phoneFrom;
	private String phoneTo;
	private String filter;
	private String content;
	private String emailTo;
	private String otp;
	private String subject;
	private String emailFromName;
}