package site.easy.to.build.crm.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpServletRequest request) {
        String homeLink = request.getContextPath().isEmpty() ? "/" : request.getContextPath() + "/";
        model.addAttribute("home", homeLink);
    }
}