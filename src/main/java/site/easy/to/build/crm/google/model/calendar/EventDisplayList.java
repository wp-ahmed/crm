package site.easy.to.build.crm.google.model.calendar;

import java.util.List;

public class EventDisplayList {
    private List<EventDisplay> items;

    public EventDisplayList(List<EventDisplay> items) {
        this.items = items;
    }

    public List<EventDisplay> getItems() {
        return items;
    }

    public void setItems(List<EventDisplay> items) {
        this.items = items;
    }
}
