package com.techx.tradex.ekycadmin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A EContractInfo.
 */
@Entity
@Table(name = "econtract_info_history")
@Data
@NoArgsConstructor
public class EContractInfoHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "contract_status")
    private String contractStatus;

    @Column(name = "customer_signatue_status")
    private String customerSignatueStatus;

    @Column(name = "securities_signature_status")
    private String securitiesSignatureStatus;

    @JsonIgnoreProperties(value = { "eKyc" }, allowSetters = true)
    @ManyToOne
    @JoinColumn(name = "e_contract_id")
    private EContract eContract;

    public EContractInfoHistory(EContractInfo eContractInfo) {
        this.createdAt = ZonedDateTime.now();
        this.contractStatus = eContractInfo.getContractStatus();
        this.customerSignatueStatus = eContractInfo.getCustomerSignatueStatus();
        this.securitiesSignatureStatus = eContractInfo.getSecuritiesSignatureStatus();
        this.eContract = eContractInfo.getEContract();
    }
}
