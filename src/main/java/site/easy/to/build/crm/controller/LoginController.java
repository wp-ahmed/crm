package site.easy.to.build.crm.controller;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.user.UserService;

import java.util.List;

@Controller
public class LoginController {

    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    public LoginController(PasswordEncoder passwordEncoder, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @RequestMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/change-password")
    public String usernameConfirmationForm() {
        return "change-password";
    }

    @PostMapping("/change-password")
    public String confirmUsername(@RequestParam("username") @Nullable String username, @RequestParam("password") @Nullable String password, RedirectAttributes redirectAttributes, HttpSession session) {
        if(username == null || username.isEmpty()) {
            redirectAttributes.addFlashAttribute("usernameError", "Username is required");
            return "redirect:/password-changing";
        }
        List<User> currUser = userService.findByUsername(username);
        if(currUser == null || currUser.isEmpty()) {
            redirectAttributes.addFlashAttribute("usernameError", "Incorrect username. Please provide a correct username");
            return "redirect:/password-changing";
        }
        if(password == null || password.isEmpty()) {
            redirectAttributes.addFlashAttribute("passwordError", "Password is required");
            return "redirect:/password-changing";
        }
        User user = currUser.get(0);
        String hashPassword = passwordEncoder.encode(password);
        user.setPassword(hashPassword);
        user.setPasswordSet(true);
        userService.save(user);
        redirectAttributes.addFlashAttribute("passwordSuccess", "You have successfully changed your password");
        return "redirect:/login";
    }

}
