package com.difisoft.nhsv.admin.domain;

import java.io.Serializable;
import java.time.ZonedDateTime;
import javax.persistence.*;

/**
 * A MarketHistoryJobResult.
 */
@Entity
@Table(name = "market_history_job_result")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class MarketHistoryJobResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "is_success")
    private Boolean isSuccess;

    @Column(name = "time_start")
    private ZonedDateTime timeStart;

    @Column(name = "time_end")
    private ZonedDateTime timeEnd;

    @Column(name = "error")
    private String error;

    @Column(name = "event_id")
    private String eventId;

    @Column(name = "symbols")
    private String symbols;

    @ManyToOne
    private User user;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public MarketHistoryJobResult id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getIsSuccess() {
        return this.isSuccess;
    }

    public MarketHistoryJobResult isSuccess(Boolean isSuccess) {
        this.setIsSuccess(isSuccess);
        return this;
    }

    public void setIsSuccess(Boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public ZonedDateTime getTimeStart() {
        return this.timeStart;
    }

    public MarketHistoryJobResult timeStart(ZonedDateTime timeStart) {
        this.setTimeStart(timeStart);
        return this;
    }

    public void setTimeStart(ZonedDateTime timeStart) {
        this.timeStart = timeStart;
    }

    public ZonedDateTime getTimeEnd() {
        return this.timeEnd;
    }

    public MarketHistoryJobResult timeEnd(ZonedDateTime timeEnd) {
        this.setTimeEnd(timeEnd);
        return this;
    }

    public void setTimeEnd(ZonedDateTime timeEnd) {
        this.timeEnd = timeEnd;
    }

    public String getError() {
        return this.error;
    }

    public MarketHistoryJobResult error(String error) {
        this.setError(error);
        return this;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getEventId() {
        return this.eventId;
    }

    public MarketHistoryJobResult eventId(String eventId) {
        this.setEventId(eventId);
        return this;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getSymbols() {
        return this.symbols;
    }

    public MarketHistoryJobResult symbols(String symbols) {
        this.setSymbols(symbols);
        return this;
    }

    public void setSymbols(String symbols) {
        this.symbols = symbols;
    }

    public User getUser() {
        return this.user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public MarketHistoryJobResult user(User user) {
        this.setUser(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MarketHistoryJobResult)) {
            return false;
        }
        return id != null && id.equals(((MarketHistoryJobResult) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "MarketHistoryJobResult{" +
            "id=" + getId() +
            ", isSuccess='" + getIsSuccess() + "'" +
            ", timeStart='" + getTimeStart() + "'" +
            ", timeEnd='" + getTimeEnd() + "'" +
            ", error='" + getError() + "'" +
            ", eventId='" + getEventId() + "'" +
            ", symbols='" + getSymbols() + "'" +
            "}";
    }
}
