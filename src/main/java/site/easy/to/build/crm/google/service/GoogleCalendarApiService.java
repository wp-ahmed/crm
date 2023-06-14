package site.easy.to.build.crm.google.service;

import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.dao.EventList;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface GoogleCalendarApiService {

    public EventList getEvents(String calendarId, OAuthUser oauthUser) throws IOException, GeneralSecurityException;

}
