package site.easy.to.build.crm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GlobalController {
    @GetMapping("/coming-soon")
    public String comingSoon(){
        return "coming-soon";
    }

    @GetMapping("/google-error")
    public String unauthorizedGoogleErrorPage(){
        return "google-error";
    }

    @GetMapping("/not-found")
    public String notFound(){
        return "error/not-found";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }

    @GetMapping("/account-inactive")
    public String accountInactive() {
        return "error/account-inactive";
    }

    @GetMapping("/account-suspended")
    public String accountSuspended() {
        return "error/account-suspended";
    }
}
