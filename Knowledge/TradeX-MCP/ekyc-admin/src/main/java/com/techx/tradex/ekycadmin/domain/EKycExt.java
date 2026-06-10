package com.techx.tradex.ekycadmin.domain;

import java.io.Serializable;
import javax.persistence.*;

/**
 * A EKycExt.
 */
@Entity
@Table(name = "ekyc_ext")
public class EKycExt implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_id")
    private String logId;

    @Lob
    @Column(name = "raw_data")
    private String rawData;

    @OneToOne
    @JoinColumn(unique = true)
    private EKyc eKyc;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EKycExt id(Long id) {
        this.id = id;
        return this;
    }

    public String getLogId() {
        return this.logId;
    }

    public EKycExt logId(String logId) {
        this.logId = logId;
        return this;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getRawData() {
        return this.rawData;
    }

    public EKycExt rawData(String rawData) {
        this.rawData = rawData;
        return this;
    }

    public void setRawData(String rawData) {
        this.rawData = rawData;
    }

    public EKyc getEKyc() {
        return this.eKyc;
    }

    public EKycExt eKyc(EKyc eKyc) {
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
        if (!(o instanceof EKycExt)) {
            return false;
        }
        return id != null && id.equals(((EKycExt) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EKycExt{" +
            "id=" + getId() +
            ", logId='" + getLogId() + "'" +
            ", rawData='" + getRawData() + "'" +
            "}";
    }
}
