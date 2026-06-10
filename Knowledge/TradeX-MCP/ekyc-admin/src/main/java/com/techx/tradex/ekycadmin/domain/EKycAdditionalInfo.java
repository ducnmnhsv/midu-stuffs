package com.techx.tradex.ekycadmin.domain;

import java.io.Serializable;
import javax.persistence.*;

/**
 * A EKycAdditionalInfo.
 */
@Entity
@Table(name = "ekyc_additional_info")
public class EKycAdditionalInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "birth_day")
    private String birthDay;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "identifier_id")
    private String identifierId;

    @Column(name = "issue_date")
    private String issueDate;

    @Column(name = "issue_place")
    private String issuePlace;

    @Column(name = "permanent_address")
    private String permanentAddress;

    @Column(name = "contact_address")
    private String contactAddress;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "position")
    private String position;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "visa_no")
    private String visaNo;

    @Column(name = "visa_issue_place")
    private String visaIssuePlace;

    @Column(name = "foreign_residence")
    private String foreignResidence;

    @Column(name = "investment_goal")
    private String investmentGoal;

    @Column(name = "risk")
    private String risk;

    @Column(name = "experienced")
    private Boolean experienced;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private EKyc eKyc;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EKycAdditionalInfo id(Long id) {
        this.id = id;
        return this;
    }

    public String getFullName() {
        return this.fullName;
    }

    public EKycAdditionalInfo fullName(String fullName) {
        this.fullName = fullName;
        return this;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getBirthDay() {
        return this.birthDay;
    }

    public EKycAdditionalInfo birthDay(String birthDay) {
        this.birthDay = birthDay;
        return this;
    }

    public void setBirthDay(String birthDay) {
        this.birthDay = birthDay;
    }

    public String getNationality() {
        return this.nationality;
    }

    public EKycAdditionalInfo nationality(String nationality) {
        this.nationality = nationality;
        return this;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getIdentifierId() {
        return this.identifierId;
    }

    public EKycAdditionalInfo identifierId(String identifierId) {
        this.identifierId = identifierId;
        return this;
    }

    public void setIdentifierId(String identifierId) {
        this.identifierId = identifierId;
    }

    public String getIssueDate() {
        return this.issueDate;
    }

    public EKycAdditionalInfo issueDate(String issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getIssuePlace() {
        return this.issuePlace;
    }

    public EKycAdditionalInfo issuePlace(String issuePlace) {
        this.issuePlace = issuePlace;
        return this;
    }

    public void setIssuePlace(String issuePlace) {
        this.issuePlace = issuePlace;
    }

    public String getPermanentAddress() {
        return this.permanentAddress;
    }

    public EKycAdditionalInfo permanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
        return this;
    }

    public void setPermanentAddress(String permanentAddress) {
        this.permanentAddress = permanentAddress;
    }

    public String getContactAddress() {
        return this.contactAddress;
    }

    public EKycAdditionalInfo contactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
        return this;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }

    public String getOccupation() {
        return this.occupation;
    }

    public EKycAdditionalInfo occupation(String occupation) {
        this.occupation = occupation;
        return this;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getPosition() {
        return this.position;
    }

    public EKycAdditionalInfo position(String position) {
        this.position = position;
        return this;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public EKycAdditionalInfo phoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getVisaNo() {
        return this.visaNo;
    }

    public EKycAdditionalInfo visaNo(String visaNo) {
        this.visaNo = visaNo;
        return this;
    }

    public void setVisaNo(String visaNo) {
        this.visaNo = visaNo;
    }

    public String getVisaIssuePlace() {
        return this.visaIssuePlace;
    }

    public EKycAdditionalInfo visaIssuePlace(String visaIssuePlace) {
        this.visaIssuePlace = visaIssuePlace;
        return this;
    }

    public void setVisaIssuePlace(String visaIssuePlace) {
        this.visaIssuePlace = visaIssuePlace;
    }

    public String getForeignResidence() {
        return this.foreignResidence;
    }

    public EKycAdditionalInfo foreignResidence(String foreignResidence) {
        this.foreignResidence = foreignResidence;
        return this;
    }

    public void setForeignResidence(String foreignResidence) {
        this.foreignResidence = foreignResidence;
    }

    public String getInvestmentGoal() {
        return this.investmentGoal;
    }

    public EKycAdditionalInfo investmentGoal(String investmentGoal) {
        this.investmentGoal = investmentGoal;
        return this;
    }

    public void setInvestmentGoal(String investmentGoal) {
        this.investmentGoal = investmentGoal;
    }

    public String getRisk() {
        return this.risk;
    }

    public EKycAdditionalInfo risk(String risk) {
        this.risk = risk;
        return this;
    }

    public void setRisk(String risk) {
        this.risk = risk;
    }

    public Boolean getExperienced() {
        return this.experienced;
    }

    public EKycAdditionalInfo experienced(Boolean experienced) {
        this.experienced = experienced;
        return this;
    }

    public void setExperienced(Boolean experienced) {
        this.experienced = experienced;
    }

    public EKyc getEKyc() {
        return this.eKyc;
    }

    public EKycAdditionalInfo eKyc(EKyc eKyc) {
        this.setEKyc(eKyc);
        return this;
    }

    public void setEKyc(EKyc eKyc) {
        this.eKyc = eKyc;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EKycAdditionalInfo)) {
            return false;
        }
        return id != null && id.equals(((EKycAdditionalInfo) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EKycAdditionalInfo{" +
            "id=" + getId() +
            ", fullName='" + getFullName() + "'" +
            ", birthDay='" + getBirthDay() + "'" +
            ", nationality='" + getNationality() + "'" +
            ", identifierId='" + getIdentifierId() + "'" +
            ", issueDate='" + getIssueDate() + "'" +
            ", issuePlace='" + getIssuePlace() + "'" +
            ", permanentAddress='" + getPermanentAddress() + "'" +
            ", contactAddress='" + getContactAddress() + "'" +
            ", occupation='" + getOccupation() + "'" +
            ", position='" + getPosition() + "'" +
            ", phoneNumber='" + getPhoneNumber() + "'" +
            ", visaNo='" + getVisaNo() + "'" +
            ", visaIssuePlace='" + getVisaIssuePlace() + "'" +
            ", foreignResidence='" + getForeignResidence() + "'" +
            ", investmentGoal='" + getInvestmentGoal() + "'" +
            ", risk='" + getRisk() + "'" +
            ", experienced='" + getExperienced() + "'" +
            "}";
    }
}
