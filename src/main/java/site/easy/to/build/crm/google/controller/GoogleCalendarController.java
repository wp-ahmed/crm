package site.easy.to.build.crm.google.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.google.dao.EventList;
import site.easy.to.build.crm.google.service.GoogleCalendarApiService;
import site.easy.to.build.crm.service.OAuthUserService;
import site.easy.to.build.crm.service.UserService;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Controller
public class GoogleCalendarController {

    @Autowired
    UserService userService;

    @Autowired
    OAuthUserService oAuthUserService;

    @Autowired
    GoogleCalendarApiService googleCalendarApiService;

    @GetMapping("/list-events")
    public String listEvents(Model model, Authentication authentication) {
        String email = ((DefaultOidcUser) authentication.getPrincipal()).getEmail();
        User user = (User) userService.findByEmail(email);
        OAuthUser oAuthUser = oAuthUserService.getOAuthUserByUser(user);
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        try {
            EventList eventList = googleCalendarApiService.getEvents("primary",oAuthUser);
            model.addAttribute("events", eventList.getItems());
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        return "event-list";
    }
}
