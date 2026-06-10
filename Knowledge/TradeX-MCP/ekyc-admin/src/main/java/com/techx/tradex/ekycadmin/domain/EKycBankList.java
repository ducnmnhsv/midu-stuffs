package com.techx.tradex.ekycadmin.domain;

import java.io.Serializable;
import javax.persistence.*;

/**
 * A EKycBankList.
 */
@Entity
@Table(name = "ekyc_bank_list")
public class EKycBankList implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bank_id")
    private String bankId;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_acc_no")
    private String bankAccNo;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "branch_id")
    private String branchId;

    @ManyToOne
    private EKyc eKyc;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EKycBankList id(Long id) {
        this.id = id;
        return this;
    }

    public String getBankId() {
        return this.bankId;
    }

    public EKycBankList bankId(String bankId) {
        this.bankId = bankId;
        return this;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getBankName() {
        return this.bankName;
    }

    public EKycBankList bankName(String bankName) {
        this.bankName = bankName;
        return this;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccNo() {
        return this.bankAccNo;
    }

    public EKycBankList bankAccNo(String bankAccNo) {
        this.bankAccNo = bankAccNo;
        return this;
    }

    public void setBankAccNo(String bankAccNo) {
        this.bankAccNo = bankAccNo;
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public EKycBankList ownerName(String ownerName) {
        this.ownerName = ownerName;
        return this;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getBranchId() {
        return this.branchId;
    }

    public EKycBankList branchId(String branchId) {
        this.branchId = branchId;
        return this;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public EKyc getEKyc() {
        return this.eKyc;
    }

    public EKycBankList eKyc(EKyc eKyc) {
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
        if (!(o instanceof EKycBankList)) {
            return false;
        }
        return id != null && id.equals(((EKycBankList) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EKycBankList{" +
            "id=" + getId() +
            ", bankId='" + getBankId() + "'" +
            ", bankName='" + getBankName() + "'" +
            ", bankAccNo='" + getBankAccNo() + "'" +
            ", ownerName='" + getOwnerName() + "'" +
            ", branchId='" + getBranchId() + "'" +
            "}";
    }
}
