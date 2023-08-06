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
}
