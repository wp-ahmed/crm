package site.easy.to.build.crm.google.model.calendar;

public class EventDateTime {
//    private String date;
    private String dateTime;

    private String timeZone;
    // Getters and setters

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
}