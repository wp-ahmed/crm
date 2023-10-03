package site.easy.to.build.crm.google.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpResponseException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.Lead;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.google.model.calendar.*;
import site.easy.to.build.crm.google.service.acess.GoogleAccessService;
import site.easy.to.build.crm.google.service.calendar.GoogleCalendarApiService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.google.util.TimeDateUtil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/employee/calendar")
public class GoogleCalendarController {

    final private GoogleCalendarApiService googleCalendarApiService;

    final private AuthenticationUtils authenticationUtils;

    final private LeadService leadService;

    final private UserService userService;

    @Autowired
    public GoogleCalendarController(GoogleCalendarApiService googleCalendarApiService, AuthenticationUtils authenticationUtils, LeadService leadService, UserService userService) {
        this.googleCalendarApiService = googleCalendarApiService;
        this.authenticationUtils = authenticationUtils;
        this.leadService = leadService;
        this.userService = userService;
    }

    @GetMapping("/list-events")
    public String listEvents(Model model, Authentication authentication) {
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        List<EventDisplay> eventDisplays;
        try {
            EventDisplayList eventDisplayList = googleCalendarApiService.getEvents("primary", oAuthUser);
            eventDisplays = eventDisplayList.getItems();
        } catch (IOException | GeneralSecurityException e) {
            return handleGoogleCalendarApiException(model,e);
        }

        model.addAttribute("eventDisplays", eventDisplays);
        return "calendar/calendar";
    }

    @GetMapping("/create-event")
    public String showCreateEventForm(Model model, Authentication authentication, @RequestParam(value = "leadId", required = false) Integer leadId) {
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        if(!oAuthUser.getGrantedScopes().contains(GoogleAccessService.SCOPE_CALENDAR)) {
            String code = "403";
            String link = "employee/settings/google-services";
            String buttonText = "Grant Access";
            String message = "Please grant the app access to Google Drive, in order to use this service";
            model.addAttribute("link",link);
            model.addAttribute("message",message);
            model.addAttribute("buttonText",buttonText);
            model.addAttribute("code",code);
            return "gmail/error";
        }
        //In case the current employee wanna schedule a meeting with a new registered / updated lead

        String email = "";
        if(leadId != null) {
            Lead lead = leadService.findByLeadId(leadId);
            email = lead.getCustomer().getEmail();
        }
        EventDisplay eventDisplay = new EventDisplay();
        eventDisplay.setTimeZoneLabels(TimeDateUtil.getTimeZonesWithLabels());
        model.addAttribute("eventDisplay",eventDisplay);
        model.addAttribute("email", email);
        model.addAttribute("leadId", leadId);
        return "calendar/event-form";
    }

    @PostMapping("/create-event")
    public String createEvent(@ModelAttribute("eventDisplay") @Valid EventDisplay eventDisplay, BindingResult bindingResult,
                              @RequestParam("emails") String emails, Authentication authentication, Model model,
                              @RequestParam(value = "leadId", required = false) Integer leadId) {
        if(bindingResult.hasErrors()){
            eventDisplay.setTimeZoneLabels(TimeDateUtil.getTimeZonesWithLabels());
            model.addAttribute("eventDisplay", eventDisplay);
            return "calendar/event-form";
        }
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        Event event = new Event();

        event.setSummary(eventDisplay.getSummary());
        String offset = eventDisplay.getTimeZone().split(",", 2)[0];
        String label =eventDisplay.getTimeZone().split(",", 2)[1].split(" ")[0];

        String startDateTime = eventDisplay.getStartDate() + "T" + eventDisplay.getStartTime() + ":00" + offset;
        String endDateTime = eventDisplay.getEndDate() + "T" + eventDisplay.getEndTime() + ":00" + offset;

        EventDateTime start = new EventDateTime();
        start.setDateTime(startDateTime);
        start.setTimeZone(label);
        event.setStart(start);

        EventDateTime end = new EventDateTime();
        end.setDateTime(endDateTime);
        end.setTimeZone(label);
        event.setEnd(end);
        event.setTimeZone(label);

        List<EventAttendee>attendees = new ArrayList<>();
        if(emails != null && !emails.isEmpty()) {
            List<String> email = List.of(emails.split(","));
            for (var em : email) {
                EventAttendee eventAttendee = new EventAttendee();
                eventAttendee.setEmail(em);
                attendees.add(eventAttendee);
            }
        }
        event.setAttendees(attendees);

        try {
            String calendarId = googleCalendarApiService.createEvent("primary", oAuthUser, event);

            if(leadId != null) {
                Lead lead = leadService.findByLeadId(leadId);
                User employee = oAuthUser.getUser();
                lead.setEmployee(employee);
                lead.setStatus("Scheduled");
                lead.setMeetingId(calendarId);
                leadService.save(lead);
            }

        } catch (IOException | GeneralSecurityException e) {
            return handleGoogleCalendarApiException(model,e);
        }
        return "redirect:/employee/calendar/list-events";
    }

    @PostMapping("/ajax-create-event")
    @ResponseBody
    public ResponseEntity<String> createEventByAjax(Authentication authentication, @RequestParam("fullStartDate") String fullStartDate,
                                                    @RequestParam("fullEndDate") String fullEndDate, @RequestParam("summary") String summary,
                                                    @RequestParam("emails") String emails) {
        HashMap<String, String> errors = getStringStringHashMap(fullStartDate, fullEndDate, summary);
        if(!errors.isEmpty()){
            ObjectMapper objectMapper = new ObjectMapper();
            String json;
            try {
                json = objectMapper.writeValueAsString(errors);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.badRequest().body(json);
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        Event event = new Event();

        ZoneId zoneId = ZoneId.systemDefault();
        String timeZoneLabel = zoneId.toString();

        event.setSummary(summary);
        EventDateTime start = new EventDateTime();
        start.setDateTime(fullStartDate);
        start.setTimeZone(timeZoneLabel);
        event.setStart(start);

        EventDateTime end = new EventDateTime();
        end.setDateTime(fullEndDate);
        end.setTimeZone(timeZoneLabel);
        event.setEnd(end);

        event.setTimeZone(timeZoneLabel);

        List<EventAttendee>attendees = new ArrayList<>();
        if(emails != null && !emails.isEmpty()) {
            List<String> email = List.of(emails.split(","));
            for (var em : email) {
                EventAttendee eventAttendee = new EventAttendee();
                eventAttendee.setEmail(em);
                attendees.add(eventAttendee);
            }
        }
        event.setAttendees(attendees);

        String eventId;

        try {
            eventId = googleCalendarApiService.createEvent("primary", oAuthUser, event);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok(eventId);
    }

    private static HashMap<String, String> getStringStringHashMap(String fullStartDate, String fullEndDate, String summary) {
        HashMap<String,String> errors = new HashMap<>();
        OffsetDateTime startDateTime = OffsetDateTime.parse(fullStartDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        OffsetDateTime endDateTime = OffsetDateTime.parse(fullEndDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        if (!endDateTime.isAfter(startDateTime)){
            errors.put("time","End time must be after start time");
        }
        if(summary ==null || summary.isEmpty()){
            errors.put("summary","Summary is required");
        }
        return errors;
    }

    @GetMapping("/update-event/{id}")
    public String showUpdateEventForm(@PathVariable("id") String eventId, Authentication authentication, Model model) {
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        EventDisplay eventDisplay;
        try {
            eventDisplay = googleCalendarApiService.getEvent("primary",oAuthUser,eventId);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("eventDisplay", eventDisplay);
        model.addAttribute("timeZones", TimeDateUtil.getTimeZonesWithLabels());
        return "calendar/event-form";
    }

    @PostMapping("/ajax-update-event")
    @ResponseBody
    public ResponseEntity<String> updateEventByAjax(@RequestParam("id") String id, @RequestParam("startTime") String startTime,
                                                    @RequestParam("endTime") String endTime, @RequestParam("summary") String summary, Authentication authentication){

        if(summary == null || summary.isEmpty()){
            return ResponseEntity.badRequest().body("Name is required");
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);

        Event updatedEvent = getEvent(startTime, endTime);
        updatedEvent.setSummary(summary);

        try {
            EventDisplay event = googleCalendarApiService.getEvent("primary",oAuthUser,id);
            updatedEvent.setAttendees(event.getAttendees());
            googleCalendarApiService.updateEvent("primary",oAuthUser, id, updatedEvent);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("Success");
    }

    private static Event getEvent(String startTime, String endTime) {
        ZoneId zoneId = ZoneId.systemDefault();
        String timeZoneLabel = zoneId.toString();

        Event updatedEvent = new Event();

        EventDateTime eventDateStartTime = new EventDateTime();
        eventDateStartTime.setDateTime(startTime);
        eventDateStartTime.setTimeZone(timeZoneLabel);

        EventDateTime eventDateEndTime = new EventDateTime();
        eventDateEndTime.setDateTime(endTime);
        eventDateEndTime.setTimeZone(timeZoneLabel);

        updatedEvent.setStart(eventDateStartTime);
        updatedEvent.setEnd(eventDateEndTime);
        return updatedEvent;
    }

    @RequestMapping("/delete-event/{id}")
    public String deleteEvent(@PathVariable("id") String eventId, Authentication authentication, Model model){
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        try {
            googleCalendarApiService.deleteEvent("primary",oAuthUser,eventId);
        } catch (IOException | GeneralSecurityException e) {
            return handleGoogleCalendarApiException(model,e);
        }
        return "redirect:/employee/calendar/list-events";
    }

    @RequestMapping("/ajax-delete-event")
    @ResponseBody
    public ResponseEntity<String> deleteEventByAjax(@RequestParam("id") String id, Authentication authentication){
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        try {
            googleCalendarApiService.deleteEvent("primary",oAuthUser,id);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("Success");
    }
    private String handleGoogleCalendarApiException(Model model, Exception e){
        String link = "";
        String buttonText = "Go Home";
        String message = "There was a problem with Google Calendar, Please try again later!";
        String code = "400";
        if (e instanceof HttpResponseException httpResponseException) {
            int statusCode = httpResponseException.getStatusCode();
            if(statusCode == 403){
                code = "403";
                link = "employee/settings/google-services";
                buttonText = "Grant Access";
                message = "Please grant the app access to Google Calendar, in order to use this service";
            }
        }
        model.addAttribute("link",link);
        model.addAttribute("message",message);
        model.addAttribute("buttonText",buttonText);
        model.addAttribute("code",code);
        return "gmail/error";
    }
}
