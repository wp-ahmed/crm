package site.easy.to.build.crm.controller;

import jakarta.annotation.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.service.customer.CustomerLoginInfoService;

@Controller
public class CustomerLoginController {

    private final CustomerLoginInfoService customerLoginInfoService;
    private final PasswordEncoder passwordEncoder;

    public CustomerLoginController(CustomerLoginInfoService customerLoginInfoService, PasswordEncoder passwordEncoder) {
        this.customerLoginInfoService = customerLoginInfoService;
        this.passwordEncoder = passwordEncoder;
    }

//
    @GetMapping("/set-password")
    public String showPasswordForm(Model model, @RequestParam("token") @Nullable String token) {
        if(token == null) {
            return "redirect:/set-password";
        }
        CustomerLoginInfo customerLoginInfo = customerLoginInfoService.findByToken(token);
        if(customerLoginInfo == null) {
            return "redirect:/set-password";
        }
        model.addAttribute("customerLoginInfo", customerLoginInfo);
        return "set-password";
    }

    @PostMapping("/set-password")
    public String setPassword(@ModelAttribute("customerLoginInfo") CustomerLoginInfo customerLoginInfo, @RequestParam("token") @Nullable String token) {
        if(token ==null){
            return "redirect:/set-password";
        }
        CustomerLoginInfo customerLoginInfo1 = customerLoginInfoService.findByToken(token);
        if(customerLoginInfo1 == null) {
            return "redirect:/set-password";
        }
        if(!customerLoginInfo1.isPasswordSet()){
            String hashPassword = passwordEncoder.encode(customerLoginInfo.getPassword());
            customerLoginInfo1.setPassword(hashPassword);
            customerLoginInfo1.setPasswordSet(true);
            customerLoginInfoService.save(customerLoginInfo1);
        }
        return "redirect:/customer-login";
    }
    @RequestMapping("/customer-login")
    public String showCustomerLoginForm() {
        return "customer-login";
    }
}
