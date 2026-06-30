package com.difisoft.nhsv.admin.domain.response;


import lombok.Data;

@Data
public class AccountInfoResponse{
    private String username;
    private String customerName;
    private String identifierNumber;
    private String identifierIssueDate;
    private String identifierIssuePlace;
    private String agencyNumber;
    private String agencyCode;
    private String agencyName;
    private String agencyRegisterDate;
    private String agencyAddress;
    private String agencyPhoneNo;
    private String email;
    private String address;
    private String phoneNumber;
    private String dateOfBirth;
    private String accountType;
    private String groupType;
    private String agencyBranch;
    private String countryCode;
    private String openBranchCode;
    private String openBranchName;
}
