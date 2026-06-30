package com.difisoft.nhsv.admin.service.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.difisoft.nhsv.admin.domain.Holiday} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class HolidayDTO implements Serializable {

    private Long id;

    @NotNull
    private Integer year;

    @NotNull
    @Size(max = 255)
    private String eventHoliday;

    private ZonedDateTime startDate;

    private ZonedDateTime endDate;

    @NotNull
    private ZonedDateTime createdAt;

    private ZonedDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getEventHoliday() {
        return eventHoliday;
    }

    public void setEventHoliday(String eventHoliday) {
        this.eventHoliday = eventHoliday;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HolidayDTO)) {
            return false;
        }

        HolidayDTO holidayDTO = (HolidayDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, holidayDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "HolidayDTO{" +
            "id=" + getId() +
            ", year=" + getYear() +
            ", eventHoliday='" + getEventHoliday() + "'" +
            ", startDate='" + getStartDate() + "'" +
            ", endDate='" + getEndDate() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}
