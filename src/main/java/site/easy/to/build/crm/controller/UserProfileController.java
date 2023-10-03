package site.easy.to.build.crm.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.entity.UserProfile;
import site.easy.to.build.crm.service.user.UserProfileService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;

@Controller
@RequestMapping("/employee/profile")
public class UserProfileController {


    private final UserService userService;
    private final UserProfileService userProfileService;
    private final AuthenticationUtils authenticationUtils;

    public UserProfileController(UserService userService, UserProfileService userProfileService, AuthenticationUtils authenticationUtils) {
        this.userService = userService;
        this.userProfileService = userProfileService;
        this.authenticationUtils = authenticationUtils;
    }


    @GetMapping("")
    public String showProfileInfo(Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        UserProfile profile = userProfileService.findByUserId(userId);

        model.addAttribute("user", user);
        model.addAttribute("profile",profile);
        return "profile";
    }

    @PostMapping("/update")
    public String updateUser(@ModelAttribute("profile") UserProfile profile, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        profile.setUser(user);
        userProfileService.save(profile);
        return "redirect:/employee/profile";
    }
}
