package site.easy.to.build.crm.google.model.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import site.easy.to.build.crm.customValidations.EndTimeAfterStartTime;
import site.easy.to.build.crm.customValidations.FutureDate;
import site.easy.to.build.crm.customValidations.SameDay;
import site.easy.to.build.crm.google.util.TimeZoneLabel;


import java.util.List;

@EndTimeAfterStartTime
@SameDay
public class EventDisplay {
    private String id;
    @NotBlank(message = "Summary is required")
    private String summary;
    @NotBlank(message = "Start date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Invalid date format. Expected format: yyyy-MM-dd")
    @FutureDate
    private String startDate;
    @NotBlank(message = "Start time is required")
    private String startTime;

    @NotBlank(message = "End date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Invalid date format. Expected format: yyyy-MM-dd")
    @FutureDate
    private String endDate;
    @NotBlank(message = "end time is required")
    private String endTime;
    @NotBlank(message = "Time zone is required")
    private String timeZone;

    private List<TimeZoneLabel> timeZoneLabels;

    private List<EventAttendee> attendees;

    public EventDisplay() {
    }

    public EventDisplay(String id, String summary, String startDate, String startTime, String endDate, String endTime, String timeZone, List<EventAttendee> attendees) {
        this.id = id;
        this.summary = summary;
        this.startDate = startDate;
        this.startTime = startTime;
        this.endDate = endDate;
        this.endTime = endTime;
        this.timeZone = timeZone;
        this.attendees = attendees;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public List<EventAttendee> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<EventAttendee> attendees) {
        this.attendees = attendees;
    }

    public List<TimeZoneLabel> getTimeZoneLabels() {
        return timeZoneLabels;
    }

    public void setTimeZoneLabels(List<TimeZoneLabel> timeZoneLabels) {
        this.timeZoneLabels = timeZoneLabels;
    }
}