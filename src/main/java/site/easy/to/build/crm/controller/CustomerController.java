package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.google.service.acess.GoogleAccessService;
import site.easy.to.build.crm.google.service.gmail.GoogleGmailApiService;
import site.easy.to.build.crm.service.contract.ContractService;
import site.easy.to.build.crm.service.customer.CustomerLoginInfoService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.ticket.TicketService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.AuthorizationUtil;
import site.easy.to.build.crm.util.EmailTokenUtils;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/employee/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final UserService userService;
    private final CustomerLoginInfoService customerLoginInfoService;
    private final AuthenticationUtils authenticationUtils;
    private final GoogleGmailApiService googleGmailApiService;
    private final Environment environment;
    private final TicketService ticketService;
    private final ContractService contractService;
    private final LeadService leadService;

    @Autowired
    public CustomerController(CustomerService customerService, UserService userService, CustomerLoginInfoService customerLoginInfoService,
                              AuthenticationUtils authenticationUtils, GoogleGmailApiService googleGmailApiService, Environment environment,
                              TicketService ticketService, ContractService contractService, LeadService leadService) {
        this.customerService = customerService;
        this.userService = userService;
        this.customerLoginInfoService = customerLoginInfoService;
        this.authenticationUtils = authenticationUtils;
        this.googleGmailApiService = googleGmailApiService;
        this.environment = environment;
        this.ticketService = ticketService;
        this.contractService = contractService;
        this.leadService = leadService;
    }

    @GetMapping("/manager/all-customers")
    public String getAllCustomers(Model model){
        List<Customer> customers;
        try {
            customers = customerService.findAll();
        } catch (Exception e){
            return "error/500";
        }
        model.addAttribute("customers",customers);
        return "customer/all-customers";
    }

    @GetMapping("/my-customers")
    public String getEmployeeCustomer(Model model, Authentication authentication){
        List<Customer> customers;

        int userId = authenticationUtils.getLoggedInUserId(authentication);
        if(userId == -1) {
            return "error/not-found";
        }
        customers = customerService.findByUserId(userId);
        model.addAttribute("customers",customers);
        return "customer/all-customers";
    }

    @GetMapping("/{id}")
    public String showCustomerDetail(@PathVariable("id") int id, Model model, Authentication authentication) {
        Customer customer = customerService.findByCustomerId(id);
        if(customer == null) {
            return "error/not-found";
        }

        User employee = customer.getUser();
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        if(!AuthorizationUtil.checkIfUserAuthorized(employee,loggedInUser)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("customer",customer);
        return "customer/customer-details";
    }

    @GetMapping("/create-customer")
    public String showCreateCustomerForm(Model model, Authentication authentication){
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if(user.isInactiveUser()) {
            return "error/account-inactive";
        }
        boolean hasGoogleGmailAccess = false;
        boolean isGoogleUser = false;
        if(!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            isGoogleUser = true;
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            if(oAuthUser.getGrantedScopes().contains(GoogleAccessService.SCOPE_GMAIL)){
                hasGoogleGmailAccess = true;
            }
        }
        model.addAttribute("customer", new Customer());
        model.addAttribute("isGoogleUser", isGoogleUser);
        model.addAttribute("hasGoogleGmailAccess", hasGoogleGmailAccess);

        return "customer/create-customer";
    }

    @PostMapping("/create-customer")
    public String createNewCustomer(@ModelAttribute("customer") @Validated Customer customer, BindingResult bindingResult,
                                    Authentication authentication, @RequestParam(value = "SendEmail", defaultValue = "false") boolean sendEmail, Model model) {

        if(bindingResult.hasErrors()) {
            boolean hasGoogleGmailAccess = false;
            boolean isGoogleUser = false;
            if(!(authentication instanceof UsernamePasswordAuthenticationToken)) {
                isGoogleUser = true;
                OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
                if(oAuthUser.getGrantedScopes().contains(GoogleAccessService.SCOPE_GMAIL)){
                    hasGoogleGmailAccess = true;
                }
            }
            model.addAttribute("isGoogleUser", isGoogleUser);
            model.addAttribute("hasGoogleGmailAccess", hasGoogleGmailAccess);
            return "customer/create-customer";
        }

        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if(user.isInactiveUser()) {
            return "error/account-inactive";
        }
        customer.setUser(user);
        customer.setCreatedAt(LocalDateTime.now());

        CustomerLoginInfo customerLoginInfo = new CustomerLoginInfo();
        customerLoginInfo.setEmail(customer.getEmail());
        String token = EmailTokenUtils.generateToken();
        customerLoginInfo.setToken(token);
        customerLoginInfo.setPasswordSet(false);

        CustomerLoginInfo customerLoginInfo1 = customerLoginInfoService.save(customerLoginInfo);
        customer.setCustomerLoginInfo(customerLoginInfo1);
        Customer createdCustomer = customerService.save(customer);
        customerLoginInfo1.setCustomer(createdCustomer);

        if (!(authentication instanceof UsernamePasswordAuthenticationToken) && sendEmail && googleGmailApiService != null) {
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            String baseUrl = environment.getProperty("app.base-url");
            String url = baseUrl + "set-password?token=" + customerLoginInfo.getToken();
            EmailTokenUtils.sendRegistrationEmail(customerLoginInfo1.getEmail(), customer.getName(), url, oAuthUser, googleGmailApiService);
        }
        return "redirect:/employee/customer/my-customers";
    }

    @PostMapping("/delete-customer/{id}")
    @Transactional
    public String deleteCustomer(@ModelAttribute("customer") Customer tempCustomer, BindingResult bindingResult ,@PathVariable("id") int id,
                                 Authentication authentication, RedirectAttributes redirectAttributes) {
        Customer customer;
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if(user.isInactiveUser()) {
            return "error/account-inactive";
        }
        try {
            if(!AuthorizationUtil.hasRole(authentication,"ROLE_MANAGER")) {
                bindingResult.rejectValue("failedErrorMessage", "error.failedErrorMessage",
                        "Sorry, you are not authorized to delete this customer. Only administrators have permission to delete customers.");
                redirectAttributes.addFlashAttribute("bindingResult", bindingResult);
                return "redirect:/employee/customer/my-customers";
            }

            customer = customerService.findByCustomerId(id);
            CustomerLoginInfo customerLoginInfo = customer.getCustomerLoginInfo();

            contractService.deleteAllByCustomer(customer);
            leadService.deleteAllByCustomer(customer);
            ticketService.deleteAllByCustomer(customer);

            customerLoginInfoService.delete(customerLoginInfo);
            customerService.delete(customer);

        } catch (Exception e){
            return "error/500";
        }
        return "redirect:/employee/customer/my-customers";
    }


}
