package site.easy.to.build.crm.google.model.calendar;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class Event {
//    @JsonIgnore
    private String id;
    private String summary;
    private EventDateTime start;
    private EventDateTime end;
//    @JsonIgnore
    private String timeZone;
    private List<EventAttendee>attendees;
    @JsonIgnore
    private String transparency;
    @JsonIgnore
    private ExtendedProperties extendedProperties;

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

    public EventDateTime getStart() {
        return start;
    }

    public void setStart(EventDateTime start) {
        this.start = start;
    }

    public EventDateTime getEnd() {
        return end;
    }

    public void setEnd(EventDateTime end) {
        this.end = end;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getTransparency() {
        return transparency;
    }

    public void setTransparency(String transparency) {
        this.transparency = transparency;
    }

    public ExtendedProperties getExtendedProperties() {
        return extendedProperties;
    }

    public void setExtendedProperties(ExtendedProperties extendedProperties) {
        this.extendedProperties = extendedProperties;
    }

    public List<EventAttendee> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<EventAttendee> attendees) {
        this.attendees = attendees;
    }

    public static class ExtendedProperties {
        private Map<String, String> shared;

        public Map<String, String> getShared() {
            return shared;
        }

        public void setShared(Map<String, String> shared) {
            this.shared = shared;
        }
    }
}

