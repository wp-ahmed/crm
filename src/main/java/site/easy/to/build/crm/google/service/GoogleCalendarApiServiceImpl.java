package site.easy.to.build.crm.google.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.dao.EventList;
import site.easy.to.build.crm.service.OAuthUserService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

@Service
public class GoogleCalendarApiServiceImpl implements GoogleCalendarApiService{

    @Autowired
    OAuthUserService oAuthUserService;

    @Autowired
    ObjectMapper objectMapper;

    public EventList getEvents(String calendarId, OAuthUser oauthUser) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oauthUser);
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(request -> {
            request.setParser(new JsonObjectParser(jsonFactory));
            request.getHeaders().setAuthorization("Bearer " + accessToken);
        });

        GenericUrl eventsUrl = new GenericUrl("https://www.googleapis.com/calendar/v3/calendars/" + calendarId + "/events");

        // Convert java.time.Instant to RFC3339 format
        String nowInRfc3339 = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        eventsUrl.put("timeMin", nowInRfc3339);
        eventsUrl.put("singleEvents", "true");
        eventsUrl.put("orderBy", "startTime");

        HttpRequest request = requestFactory.buildGetRequest(eventsUrl);
        HttpResponse response = request.execute();
        String jsonResponse = response.parseAsString();
        return objectMapper.readValue(jsonResponse, EventList.class);
    }
}
