package site.easy.to.build.crm.google.service.calendar;

import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.model.calendar.Event;
import site.easy.to.build.crm.google.model.calendar.EventDisplay;
import site.easy.to.build.crm.google.model.calendar.EventDisplayList;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface GoogleCalendarApiService {

    public EventDisplayList getEvents(String calendarId, OAuthUser oauthUser) throws IOException, GeneralSecurityException;

    public String createEvent(String calendarId, OAuthUser oauthUser, Event event) throws IOException, GeneralSecurityException;

    public Event updateEvent(String calendarId, OAuthUser oauthUser, String eventId, Event updatedEvent) throws IOException, GeneralSecurityException;

    public void deleteEvent(String calendarId, OAuthUser oauthUser, String eventId) throws IOException, GeneralSecurityException;

    EventDisplay getEvent(String primary, OAuthUser oAuthUser, String eventId) throws IOException, GeneralSecurityException;
}
