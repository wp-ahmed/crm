package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.UserService;

import java.time.LocalDateTime;

@Controller
public class RegisterController {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserService userService;

    @GetMapping("/register")
    public String fillRegistrationInfo(Model model) {
        User user = new User();
        model.addAttribute("user",user);
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user) {

        // Set the created_at and updated_at timestamps
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        String hashPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        userService.save(user);

        return "redirect:/login";
    }
}
