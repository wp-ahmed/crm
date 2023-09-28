package site.easy.to.build.crm.controller;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.entity.settings.ContractEmailSettings;
import site.easy.to.build.crm.entity.settings.EmailSettings;
import site.easy.to.build.crm.entity.settings.LeadEmailSettings;
import site.easy.to.build.crm.entity.settings.TicketEmailSettings;
import site.easy.to.build.crm.service.customer.CustomerLoginInfoService;
import site.easy.to.build.crm.service.settings.ContractEmailSettingsService;
import site.easy.to.build.crm.service.settings.LeadEmailSettingsService;
import site.easy.to.build.crm.service.settings.TicketEmailSettingsService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.DatabaseUtil;
import site.easy.to.build.crm.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Controller
@RequestMapping("/customer/settings")
public class CustomerEmailSettingController {
    private final EntityManager entityManager;
    private final CustomerLoginInfoService customerLoginInfoService;
    private final AuthenticationUtils authenticationUtils;
    private final ContractEmailSettingsService contractEmailSettingsService;
    private final LeadEmailSettingsService leadEmailSettingsService;
    private final TicketEmailSettingsService ticketEmailSettingsService;

    public CustomerEmailSettingController(EntityManager entityManager, CustomerLoginInfoService customerLoginInfoService, AuthenticationUtils authenticationUtils,
                                          ContractEmailSettingsService contractEmailSettingsService, LeadEmailSettingsService leadEmailSettingsService,
                                          TicketEmailSettingsService ticketEmailSettingsService) {
        this.entityManager = entityManager;
        this.customerLoginInfoService = customerLoginInfoService;
        this.authenticationUtils = authenticationUtils;
        this.contractEmailSettingsService = contractEmailSettingsService;
        this.leadEmailSettingsService = leadEmailSettingsService;
        this.ticketEmailSettingsService = ticketEmailSettingsService;
    }


    @GetMapping("/email/{entity}")
    public String showEntityEmailNotificationSettings(@PathVariable("entity") String entity, Model model, Authentication authentication, HttpSession session) {

        String className = entity.substring(0, 1).toUpperCase() + entity.substring(1);
        Set<Class<?>> entityNamesWithTriggerTable = DatabaseUtil.getAllEntitiesWithTriggerTable(entityManager);


        Optional<Class<?>> foundClass = entityNamesWithTriggerTable.stream()
                .filter(entityClass -> entityClass.getSimpleName().equals(className))
                .findFirst();
        if (foundClass.isEmpty()) {
            return "error/not-found";
        }
        int customerId = authenticationUtils.getLoggedInUserId(authentication);

        List<String> notifications = DatabaseUtil.getColumnNames(entityManager, foundClass.get());
        Map<String,String> namesAndFields = new HashMap<>();
        for(String notification : notifications) {
            namesAndFields.put(notification, StringUtils.replaceCharToCamelCase(notification,' '));
        }
        EmailSettings emailSettings = null;

        switch (entity) {
            case "ticket" -> {
                emailSettings = ticketEmailSettingsService.findByCustomerId(customerId);
                if (emailSettings == null) {
                    emailSettings = new TicketEmailSettings();
                }
            }
            case "lead" -> {
                emailSettings = leadEmailSettingsService.findByCustomerId(customerId);
                if (emailSettings == null) {
                    emailSettings = new LeadEmailSettings();
                }
            }
            case "contract" -> {
                emailSettings = contractEmailSettingsService.findByCustomerId(customerId);

                if (emailSettings == null) {
                    emailSettings = new ContractEmailSettings();
                }
            }
        }
        model.addAttribute("entity", entity);
        model.addAttribute("emailSettings", emailSettings);
        model.addAttribute("namesAndFields", namesAndFields);

        return "settings/customer-email";
    }
    @PostMapping("/email/{entity}")
    public String saveEmailSettings(@PathVariable("entity") String entity,
                                    @RequestParam Map<String, String> formParams,
                                    Authentication authentication) {

        int customerId = authenticationUtils.getLoggedInUserId(authentication);
        CustomerLoginInfo customerLoginInfo = customerLoginInfoService.findById(customerId);

        switch (entity) {
            case "ticket" -> {
                TicketEmailSettings ticketEmailSettings = new TicketEmailSettings();
                processEmailSettings(ticketEmailSettings, formParams);
                ticketEmailSettings.setCustomerLoginInfo(customerLoginInfo);
                ticketEmailSettingsService.save(ticketEmailSettings);
            }
            case "lead" -> {
                LeadEmailSettings leadEmailSettings = new LeadEmailSettings();
                processEmailSettings(leadEmailSettings, formParams);
                leadEmailSettings.setCustomerLoginInfo(customerLoginInfo);
                leadEmailSettingsService.save(leadEmailSettings);
            }
            case "contract" -> {
                ContractEmailSettings contractEmailSettings = new ContractEmailSettings();
                processEmailSettings(contractEmailSettings, formParams);
                contractEmailSettings.setCustomerLoginInfo(customerLoginInfo);
                contractEmailSettingsService.save(contractEmailSettings);
            }
            default -> {
                // Handle invalid entity case or return an error
                return "error/not-found";
            }
        }

        return "redirect:/customer/settings/email/"+entity;
    }

    private void processEmailSettings(EmailSettings emailSettings, Map<String, String> formParams) {
        Class<?> emailSettingsClass = emailSettings.getClass();

        for (Map.Entry<String, String> entry : formParams.entrySet()) {
            if (entry.getKey().startsWith("_") || entry.getKey().equals("csrf") || (entry.getKey().equals("id") && entry.getValue().isEmpty()) ) {
                continue;
            }

            String propertyName = StringUtils.replaceCharToCamelCase(entry.getKey(),'_');
            String setterMethodName = "set" + StringUtils.capitalizeFirstLetter(propertyName);

            try {
                Method setterMethod;
                if(entry.getKey().equals("id")) {
                    Integer num = Integer.parseInt(entry.getValue());
                    setterMethod = emailSettingsClass.getMethod(setterMethodName, num.getClass());
                    setterMethod.invoke(emailSettings,num);
                }else {
                    Boolean checked = Boolean.valueOf(entry.getValue());
                    setterMethod = emailSettingsClass.getMethod(setterMethodName, checked.getClass());
                    setterMethod.invoke(emailSettings, checked);
                }

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
