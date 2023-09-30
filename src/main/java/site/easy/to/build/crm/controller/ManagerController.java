package site.easy.to.build.crm.controller;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.google.service.gmail.GoogleGmailApiService;
import site.easy.to.build.crm.service.role.RoleService;
import site.easy.to.build.crm.service.user.UserProfileService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.EmailTokenUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class ManagerController {
    private final AuthenticationUtils authenticationUtils;
    private final UserProfileService userProfileService;
    private final UserService userService;
    private final Environment environment;
    private final GoogleGmailApiService googleGmailApiService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ManagerController(AuthenticationUtils authenticationUtils, UserProfileService userProfileService, UserService userService,
                             Environment environment, GoogleGmailApiService googleGmailApiService, RoleService roleService, PasswordEncoder passwordEncoder) {
        this.authenticationUtils = authenticationUtils;
        this.userProfileService = userProfileService;
        this.userService = userService;
        this.environment = environment;
        this.googleGmailApiService = googleGmailApiService;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/manager/all-users")
    public String showAllUsers(Model model, Authentication authentication) {
        List<User> profiles = userService.findAll();
        int currentUserId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(currentUserId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        profiles.removeIf(profile -> profile.getId() == currentUserId);

        model.addAttribute("profiles",profiles);
        return "manager/all-users";
    }

    @GetMapping("/manager/show-user/{id}")
    public String showUserInfo(@PathVariable("id") int id, Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        User user = userService.findById(id);
        if(user == null) {
            return "error/not-found";
        }
        UserProfile profile = user.getUserProfile();
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        return "manager/show-user";
    }

    @GetMapping("/manager/register-user")
    public String showRegistrationForm(Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        boolean gmailAccess = false;
        boolean isGoogleUser = !(authentication instanceof UsernamePasswordAuthenticationToken);
        if (isGoogleUser) {
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            gmailAccess = authenticationUtils.checkIfAppHasAccess("https://www.googleapis.com/auth/gmail.modify", oAuthUser);
        }
        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("gmailAccess",gmailAccess);
        model.addAttribute("isGoogleUser",isGoogleUser);
        model.addAttribute("roles",roles);
        model.addAttribute("user",new User());
        return "manager/register-user";
    }

    @PostMapping("/manager/register-user")
    public String registerUser(@ModelAttribute("user") @Validated(User.ValidationGroupInclusion.class) User user, BindingResult bindingResult,
                               @RequestParam("role") int roleId, Model model, Authentication authentication){
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        if(bindingResult.hasErrors()){
            boolean gmailAccess = false;
            boolean isGoogleUser = !(authentication instanceof UsernamePasswordAuthenticationToken);
            if (isGoogleUser) {
                OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
                gmailAccess = authenticationUtils.checkIfAppHasAccess("https://www.googleapis.com/auth/gmail.modify", oAuthUser);
            }
            List<Role> roles = roleService.getAllRoles();
            model.addAttribute("roles",roles);
            model.addAttribute("gmailAccess",gmailAccess);
            model.addAttribute("isGoogleUser",isGoogleUser);
            return "manager/register-user";
        }

        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);

        Optional<Role> role = roleService.findById(roleId);
        if(role.isEmpty()) {
            return "error/400";
        }

        role.ifPresent(value -> user.setRoles(List.of(value)));

        String token = EmailTokenUtils.generateToken();
        user.setToken(token);

        String baseUrl = environment.getProperty("app.base-url") + "set-employee-password?token=" + token;
        String name = user.getEmail().split("@")[0];
        if(googleGmailApiService != null) {
            EmailTokenUtils.sendRegistrationEmail(user.getEmail(), name, baseUrl, oAuthUser, googleGmailApiService);
        }
        user.setUsername(name);
        user.setPasswordSet(false);
        user.setCreatedAt(LocalDateTime.now());
        User createdUser = userService.save(user);
        UserProfile userProfile = new UserProfile();
        userProfile.setStatus(user.getStatus());
        userProfile.setFirstName(name);
        userProfile.setUser(createdUser);
        userProfileService.save(userProfile);
        return "redirect:/manager/all-users";
    }

    @GetMapping("/manager/update-user/{id}")
    public String showUserUpdatingForm(@PathVariable("id") int id, Model model, HttpSession session, Authentication authentication) {
        User user = userService.findById(id);
        int managerId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(managerId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        String sessionName = managerId + "manager-update-user";
        session.setAttribute(sessionName,id);
        List<Role> roles = roleService.getAllRoles();
        model.addAttribute("roles",roles);
        model.addAttribute("user", user);
        return "manager/update-user";
    }

    @PostMapping("/manager/update-user")
    public String updateUserInfo(@ModelAttribute("user") @Validated(User.ManagerUpdateValidationGroupInclusion.class) User user, BindingResult bindingResult,
                                 @RequestParam("role") int roleId, HttpSession session, Authentication authentication, Model model) {

        int managerId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(managerId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        String sessionName = managerId + "manager-update-user";

        if(session.getAttribute(sessionName) == null) {
            return "error/not-found";
        }

        int employeeId = (int) session.getAttribute(sessionName);

        if(bindingResult.hasErrors()) {
            List<Role> roles = roleService.getAllRoles();
            User employee = userService.findById(employeeId);
            model.addAttribute("roles",roles);
            model.addAttribute("user", employee);
            return "manager/update-user";
        }

        session.removeAttribute(sessionName);

        User employee = userService.findById(employeeId);
        employee.setStatus(user.getStatus());
        employee.setUpdatedAt(LocalDateTime.now());
        Optional<Role> role = roleService.findById(roleId);

        if(role.isEmpty()) {
            return "error/400";
        }

        List<Role> roles = new ArrayList<>();
        roles.add(role.get());
        employee.setRoles(roles);
        userService.save(employee);
        return "redirect:/manager/all-users";
    }

    @GetMapping("/set-employee-password")
    public String showPasswordForm(Model model, @RequestParam("token") @Nullable String token, RedirectAttributes redirectAttributes) {
        if(token == null) {
            redirectAttributes.addFlashAttribute("tokenError", "Incorrect token. Please check with your manager");
            return "redirect:/login";
        }
        User user = userService.findByToken(token);
        if(user == null) {
            redirectAttributes.addFlashAttribute("tokenError", "Incorrect token. Please check with your manager");
            return "redirect:/login";
        }
        if(user.isPasswordSet()) {
            redirectAttributes.addFlashAttribute("passwordSetError", "Password has already been set.");
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        model.addAttribute("username", user.getUsername());
        return "set-employee-password";
    }

    @PostMapping("/set-employee-password")
    public String setPassword(@ModelAttribute("user") @Validated(User.SetEmployeePasswordValidation.class) User user, BindingResult bindingResult, Model model,
                              @RequestParam("token") @Nullable String token, RedirectAttributes redirectAttributes){
        if(token ==null){
            redirectAttributes.addFlashAttribute("tokenError", "Incorrect token. Please check with your manager.");
            return "redirect:/login";
        }
        User currUser = userService.findByToken(token);
        if(currUser == null) {
            redirectAttributes.addFlashAttribute("tokenError", "Incorrect token. Please check with your manager.");
            return "redirect:/login";
        }

        if(bindingResult.hasErrors()) {
            user.setUsername(currUser.getUsername());
            user.setEmail(currUser.getEmail());
            user.setToken(currUser.getToken());
            user.setStatus(currUser.getStatus());
            user.setCreatedAt(currUser.getCreatedAt());
            user.setPasswordSet(false);
            user.setUserProfile(currUser.getUserProfile());
            user.setRoles(currUser.getRoles());
            model.addAttribute("username",currUser.getUsername());
            return "set-employee-password";
        }
        if(!currUser.isPasswordSet()) {
            String hashPassword = passwordEncoder.encode(user.getPassword());
            currUser.setPassword(hashPassword);
            currUser.setPasswordSet(true);
            userService.save(currUser);
        }
        return "redirect:/login";
    }
}
