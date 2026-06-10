package com.techx.tradex.ekycadmin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;

/**
 * A PublicCoop.
 */
@Entity
@Table(name = "public_coop")
public class PublicCoop implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "stock")
    private String stock;

    @Column(name = "position")
    private String position;

    @ManyToOne
    @JsonIgnoreProperties(value = { "eKyc" }, allowSetters = true)
    private EKycAdditionalInfo eKycAdditionalInfo;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PublicCoop id(Long id) {
        this.id = id;
        return this;
    }

    public String getCompanyName() {
        return this.companyName;
    }

    public PublicCoop companyName(String companyName) {
        this.companyName = companyName;
        return this;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getStock() {
        return this.stock;
    }

    public PublicCoop stock(String stock) {
        this.stock = stock;
        return this;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public String getPosition() {
        return this.position;
    }

    public PublicCoop position(String position) {
        this.position = position;
        return this;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public EKycAdditionalInfo getEKycAdditionalInfo() {
        return this.eKycAdditionalInfo;
    }

    public PublicCoop eKycAdditionalInfo(EKycAdditionalInfo eKycAdditionalInfo) {
        this.setEKycAdditionalInfo(eKycAdditionalInfo);
        return this;
    }

    public void setEKycAdditionalInfo(EKycAdditionalInfo eKycAdditionalInfo) {
        this.eKycAdditionalInfo = eKycAdditionalInfo;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PublicCoop)) {
            return false;
        }
        return id != null && id.equals(((PublicCoop) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "PublicCoop{" +
            "id=" + getId() +
            ", companyName='" + getCompanyName() + "'" +
            ", stock='" + getStock() + "'" +
            ", position='" + getPosition() + "'" +
            "}";
    }
}
