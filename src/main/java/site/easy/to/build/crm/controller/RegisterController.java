package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import site.easy.to.build.crm.entity.Role;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.entity.UserProfile;
import site.easy.to.build.crm.service.role.RoleService;
import site.easy.to.build.crm.service.user.UserProfileService;
import site.easy.to.build.crm.service.user.UserService;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class RegisterController {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService;

    @Autowired
    UserProfileService userProfileService;
    @Autowired
    RoleService roleService;

    @GetMapping("/register")
    public String fillRegistrationInfo(Model model) {
        User user = new User();
        model.addAttribute("user",user);
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") @Validated(User.RegistrationValidation.class) User user, BindingResult bindingResult, Model model) {

        if(bindingResult.hasErrors()) {
            return "register";
        }
        // Set the created_at and updated_at timestamps
        LocalDateTime now = LocalDateTime.now();
        long countUsers = userService.countAllUsers();
        Role role;
        if(countUsers == 0) {
           role = roleService.findByName("ROLE_MANAGER");
            user.setStatus("active");
        } else {
            role = roleService.findByName("ROLE_EMPLOYEE");
            user.setStatus("inactive");
        }
        user.setRoles(List.of(role));
        user.setCreatedAt(now);
        user.setPasswordSet(true);
        String hashPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        User createdUser = userService.save(user);
        UserProfile profile = new UserProfile();
        profile.setUser(createdUser);
        profile.setFirstName(user.getUsername());
        userProfileService.save(profile);

        return "redirect:/login";
    }
}
