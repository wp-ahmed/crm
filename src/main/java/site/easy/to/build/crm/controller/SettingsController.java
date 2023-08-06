package site.easy.to.build.crm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.google.service.acess.GoogleAccessService;
import site.easy.to.build.crm.service.OAuthUserService;
import site.easy.to.build.crm.service.UserService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/settings")
public class SettingsController {

    final GoogleAccessService googleAccessService;

    final UserService userService;

    final OAuthUserService oAuthUserService;

    public SettingsController(GoogleAccessService googleAccessService, UserService userService, OAuthUserService oAuthUserService) {
        this.googleAccessService = googleAccessService;
        this.userService = userService;
        this.oAuthUserService = oAuthUserService;
    }

    @GetMapping("/google-services")
    public String accessSettings(Model model, Authentication authentication) throws IOException {
        if(authentication instanceof UsernamePasswordAuthenticationToken) {
            model.addAttribute("oAuthUser",null);
            return "google-settings";
        }
        String email = ((DefaultOidcUser) authentication.getPrincipal()).getEmail();
        User user = userService.findByEmail(email);
        OAuthUser oAuthUser = user.getOauthUser();


        List<String> scopesToCheck = Arrays.asList(
                GoogleAccessService.SCOPE_CALENDAR,
                GoogleAccessService.SCOPE_GMAIL,
                GoogleAccessService.SCOPE_DRIVE
        );

        googleAccessService.verifyAccessAndHandleRevokedToken(oAuthUser, user, scopesToCheck);
        oAuthUserService.save(oAuthUser, user);
        model.addAttribute("oAuthUser",oAuthUser);
        return "google-settings";
    }

    @PostMapping("/grant-access")
    public RedirectView grantGoogleAccess(Authentication authentication, @Autowired HttpSession session,
                                          Model model,
                                          @RequestParam(required = false) boolean grantCalendarAccess,
                                          @RequestParam(required = false) boolean grantGmailAccess,
                                          @RequestParam(required = false) boolean grantDriveAccess) {
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return new RedirectView("/google-error");
        }
        return googleAccessService.grantGoogleAccess(authentication, session, grantCalendarAccess, grantGmailAccess, grantDriveAccess);
    }

    @GetMapping("/handle-granted-access")
    public String handleGrantedAccess(@Autowired HttpSession session, @RequestParam(value = "error", required = false) String error,
                                      @RequestParam(value = "code", required = false) String authCode, @RequestParam String state,
                                      HttpServletRequest request, Authentication authentication) throws IOException {
        return googleAccessService.handleGrantedAccess(session, error, authCode, state, authentication);
    }
}
