package site.easy.to.build.crm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.entity.UserProfile;
import site.easy.to.build.crm.service.user.OAuthUserService;
import site.easy.to.build.crm.service.user.UserProfileService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;

@Controller
public class AccountConnectionController {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final OAuthUserService oAuthUserService;
    private final AuthenticationUtils authenticationUtils;
    private final UserProfileService userProfileService;
    private final HttpSession session;
    public AccountConnectionController(PasswordEncoder passwordEncoder, UserService userService, OAuthUserService oAuthUserService, AuthenticationUtils authenticationUtils, UserProfileService userProfileService, HttpSession session) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.oAuthUserService = oAuthUserService;
        this.authenticationUtils = authenticationUtils;
        this.userProfileService = userProfileService;
        this.session = session;
    }


    @GetMapping("/connect-accounts")
    public String showConnectAccountsForm(Model model, Authentication authentication) {
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        if(oAuthUser == null || oAuthUser.getUser() != null) {
            return "redirect:/";
        }
        model.addAttribute("user", new User());
        return "connect-accounts";
    }


    @PostMapping("/connect-accounts")
    public String connectAccounts(@ModelAttribute("user") User user, Authentication authentication,
                                  HttpServletRequest request, RedirectAttributes redirectAttributes) {
        boolean previouslyUsedRegularAccount = session.getAttribute("loggedInUserId") != null;
        int userId = (previouslyUsedRegularAccount) ?  (int) session.getAttribute("loggedInUserId") : -1;

        boolean regularAccountVerified = verifyRegularAccount(user.getUsername(), user.getPassword(), userId);
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);

        if(regularAccountVerified) {
            User currentUser = userService.findByUsername(user.getUsername()).get(0);
            oAuthUserService.save(oAuthUser, currentUser);
            session.removeAttribute("loggedInUserId");
            redirectAttributes.addFlashAttribute("successMessage", "Accounts connected successfully.");
            return "redirect:/";
        }
        redirectAttributes.addFlashAttribute("errorMessage", "Invalid account credentials.");
        return "redirect:/connect-accounts";
    }

    private boolean verifyRegularAccount(String username, String password,int sessionUserId) {
        User user = userService.findByUsername(username).get(0);
        if(user==null || user.getId() != sessionUserId){
            return false;
        }
        return passwordEncoder.matches(password, user.getPassword());
    }

    @PostMapping("/discard-accounts")
    public String discardAccounts(Authentication authentication) {
        session.removeAttribute("loggedInUserId");

        String email = ((DefaultOidcUser) authentication.getPrincipal()).getEmail();
        String img = ((DefaultOidcUser) authentication.getPrincipal()).getPicture();
        String firstName = ((DefaultOidcUser) authentication.getPrincipal()).getGivenName();
        String lastName = ((DefaultOidcUser) authentication.getPrincipal()).getFamilyName();
        String username = email.split("@")[0];

        User user = new User();
        UserProfile userProfile = new UserProfile();
        userProfile.setFirstName(firstName);
        userProfile.setLastName(lastName);
        userProfile.setOathUserImageLink(img);
        user.setEmail(email);
        user.setUsername(username);
        User createdUser = userService.save(user);
        userProfile.setUser(createdUser);
        userProfileService.save(userProfile);
//        user.setUserProfile(userProfile);

        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        oAuthUserService.save(oAuthUser, user);

        return "redirect:/";
    }
}
