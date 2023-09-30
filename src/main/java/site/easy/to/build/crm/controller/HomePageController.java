package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.google.model.calendar.EventDisplay;
import site.easy.to.build.crm.google.model.calendar.EventDisplayList;
import site.easy.to.build.crm.google.service.acess.GoogleAccessService;
import site.easy.to.build.crm.google.service.calendar.GoogleCalendarApiService;
import site.easy.to.build.crm.service.contract.ContractService;
import site.easy.to.build.crm.service.customer.CustomerLoginInfoService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.ticket.TicketService;
import site.easy.to.build.crm.service.weather.WeatherService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.AuthorizationUtil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Controller
public class HomePageController {
    private final TicketService ticketService;
    private final CustomerService customerService;
    private final ContractService contractService;
    private final LeadService leadService;
    private final WeatherService weatherService;
    private final AuthenticationUtils authenticationUtils;
    private final GoogleCalendarApiService googleCalendarApiService;
    private final CustomerLoginInfoService customerLoginInfoService;

    @Autowired
    public HomePageController(TicketService ticketService, CustomerService customerService, ContractService contractService, LeadService leadService,
                              WeatherService weatherService, AuthenticationUtils authenticationUtils, GoogleCalendarApiService googleCalendarApiService,
                              CustomerLoginInfoService customerLoginInfoService) {
        this.ticketService = ticketService;
        this.customerService = customerService;
        this.contractService = contractService;
        this.leadService = leadService;
        this.weatherService = weatherService;
        this.authenticationUtils = authenticationUtils;
        this.googleCalendarApiService = googleCalendarApiService;
        this.customerLoginInfoService = customerLoginInfoService;
    }

    @GetMapping("/")
    public String showHomePage(Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        WeatherData weatherData = weatherService.getWeatherData("cairo");

        List<Ticket> tickets;
        List<Lead> leads;
        List<Customer> customers;
        List<Contract> contracts;
        long countTickets;
        long countLeads;
        long countContracts;

        if(AuthorizationUtil.hasRole(authentication,"ROLE_CUSTOMER")) {
            String email = customerLoginInfoService.findById(userId).getEmail();
            Customer customer = customerService.findByEmail(email);
            userId = customer.getCustomerId();
            tickets = ticketService.getRecentCustomerTickets(userId, 10);
            countTickets = ticketService.countByCustomerCustomerId(userId);

            leads = leadService.getRecentCustomerLeads(userId, 10);
            countLeads = leadService.countByCustomerId(userId);

            contracts = contractService.getRecentCustomerContracts(userId, 10);
            countContracts = contractService.countByCustomerId(userId);

        } else {
            tickets = ticketService.getRecentEmployeeTickets(userId, 10);
            leads = leadService.getRecentLeadsByEmployee(userId, 10);
            customers = customerService.getRecentCustomers(userId, 10);
            contracts = contractService.getRecentContracts(userId, 10);

            countTickets = ticketService.countByEmployeeId(userId);
            countLeads = leadService.countByEmployeeId(userId);
            Long countCustomers = customerService.countByUserId(userId);
            countContracts = contractService.countByUserId(userId);
            List<EventDisplay> eventDisplays = null;
            boolean hasCalendarAccess = false;
            boolean isGoogleUser = false;
            if (!(authentication instanceof UsernamePasswordAuthenticationToken) && googleCalendarApiService != null) {
                isGoogleUser = true;
                OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
                if (oAuthUser.getGrantedScopes().contains(GoogleAccessService.SCOPE_CALENDAR)) {
                    try {
                        hasCalendarAccess = true;
                        EventDisplayList eventDisplayList = googleCalendarApiService.getEvents("primary", oAuthUser);
                        eventDisplays = eventDisplayList.getItems();
                    } catch (IOException | GeneralSecurityException e) {
                        throw new RuntimeException("error" + e);
                    }
                }
            }
            model.addAttribute("customers", customers);
            model.addAttribute("countCustomers", countCustomers);
            model.addAttribute("eventDisplays", eventDisplays);
            model.addAttribute("hasCalendarAccess", hasCalendarAccess);
            model.addAttribute("isGoogleUser", isGoogleUser);

        }
        model.addAttribute("tickets", tickets);
        model.addAttribute("leads", leads);
        model.addAttribute("contracts", contracts);
        model.addAttribute("weatherData", weatherData);
        model.addAttribute("countTickets", countTickets);
        model.addAttribute("countLeads", countLeads);
        model.addAttribute("countContracts", countContracts);

        return (AuthorizationUtil.hasRole(authentication,"ROLE_CUSTOMER")) ? "customer-dashboard" : "index";
    }
}
