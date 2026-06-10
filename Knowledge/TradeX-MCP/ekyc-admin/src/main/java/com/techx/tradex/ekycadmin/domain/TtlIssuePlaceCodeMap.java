package com.techx.tradex.ekycadmin.domain;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.*;

/**
 * A TtlIssuePlaceCodeMap.
 */
@Entity
@Table(name = "ttl_issue_place_code_map")
public class TtlIssuePlaceCodeMap implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "code", nullable = false)
    private String code;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "enable_regex")
    private Boolean enableRegex;

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TtlIssuePlaceCodeMap id(Long id) {
        this.id = id;
        return this;
    }

    public String getCode() {
        return this.code;
    }

    public TtlIssuePlaceCodeMap code(String code) {
        this.code = code;
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return this.name;
    }

    public TtlIssuePlaceCodeMap name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnableRegex() {
        return this.enableRegex;
    }

    public TtlIssuePlaceCodeMap enableRegex(Boolean enableRegex) {
        this.enableRegex = enableRegex;
        return this;
    }

    public void setEnableRegex(Boolean enableRegex) {
        this.enableRegex = enableRegex;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TtlIssuePlaceCodeMap)) {
            return false;
        }
        return id != null && id.equals(((TtlIssuePlaceCodeMap) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "TtlIssuePlaceCodeMap{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", name='" + getName() + "'" +
            ", enableRegex='" + getEnableRegex() + "'" +
            "}";
    }
}
