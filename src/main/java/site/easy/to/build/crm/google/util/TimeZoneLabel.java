package site.easy.to.build.crm.google.util;

public class TimeZoneLabel {
    private String label;
    private String offset;

    public TimeZoneLabel(String label, String offset) {
        this.label = label;
        this.offset = offset;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getOffset() {
        return offset;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }
}
