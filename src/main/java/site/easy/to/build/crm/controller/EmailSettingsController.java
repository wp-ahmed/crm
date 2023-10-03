package site.easy.to.build.crm.controller;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.entity.settings.*;
import site.easy.to.build.crm.service.email.EmailTemplateService;
import site.easy.to.build.crm.service.settings.ContractEmailSettingsService;
import site.easy.to.build.crm.service.settings.LeadEmailSettingsService;
import site.easy.to.build.crm.service.settings.TicketEmailSettingsService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.DatabaseUtil;
import site.easy.to.build.crm.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Controller
@RequestMapping("/employee/settings")
public class EmailSettingsController {

    private final AuthenticationUtils authenticationUtils;
    private final EntityManager entityManager;
    private final UserService userService;
    private final EmailTemplateService emailTemplateService;
    private final LeadEmailSettingsService leadEmailSettingsService;
    private final TicketEmailSettingsService ticketEmailSettingsService;
    private final ContractEmailSettingsService contractEmailSettingsService;

    public EmailSettingsController(AuthenticationUtils authenticationUtils, EntityManager entityManager, UserService userService,
                                   EmailTemplateService emailTemplateService, LeadEmailSettingsService leadEmailSettingsService,
                                   TicketEmailSettingsService ticketEmailSettingsService, ContractEmailSettingsService contractEmailSettingsService) {
        this.authenticationUtils = authenticationUtils;
        this.entityManager = entityManager;
        this.userService = userService;
        this.emailTemplateService = emailTemplateService;
        this.leadEmailSettingsService = leadEmailSettingsService;
        this.ticketEmailSettingsService = ticketEmailSettingsService;
        this.contractEmailSettingsService = contractEmailSettingsService;
    }

    @GetMapping("/email/{entity}")
    public String showEntityEmailNotificationSettings(@PathVariable("entity") String entity, Model model, Authentication authentication, HttpSession session) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if(user.isInactiveUser()) {
            return "error/account-inactive";
        }

        String className = entity.substring(0, 1).toUpperCase() + entity.substring(1);
        Set<Class<?>> entityNamesWithTriggerTable = DatabaseUtil.getAllEntitiesWithTriggerTable(entityManager);


        Optional<Class<?>> foundClass = entityNamesWithTriggerTable.stream()
                .filter(entityClass -> entityClass.getSimpleName().equals(className))
                .findFirst();
        if (foundClass.isEmpty()) {
            return "error/not-found";
        }

        boolean gmailAccess = false;
        boolean isGoogleUser = !(authentication instanceof UsernamePasswordAuthenticationToken);
        if (isGoogleUser) {
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            gmailAccess = authenticationUtils.checkIfAppHasAccess("https://www.googleapis.com/auth/gmail.modify", oAuthUser);
        }

        List<String> notifications = DatabaseUtil.getColumnNames(entityManager, foundClass.get());
        Map<String, String> namesAndFields = new HashMap<>();
        for (String notification : notifications) {
            namesAndFields.put(notification, StringUtils.replaceCharToCamelCase(notification, ' '));
        }
        EmailSettings emailSettings = null;

        // Determine the appropriate email settings object based on the entity type
        switch (entity) {
            case "ticket" -> {
                emailSettings = ticketEmailSettingsService.findByUserId(userId);
                if (emailSettings == null) {
                    emailSettings = new TicketEmailSettings();
                }
            }
            case "lead" -> {
                emailSettings = leadEmailSettingsService.findByUserId(userId);
                if (emailSettings == null) {
                    emailSettings = new LeadEmailSettings();
                }
            }
            case "contract" -> {
                emailSettings = contractEmailSettingsService.findByUserId(userId);

                if (emailSettings == null) {
                    emailSettings = new ContractEmailSettings();
                }
            }
            default -> {
                // Handle invalid entity case or return an error
                return "error/not-found";
            }
        }

        List<EmailTemplate> emailTemplates = emailTemplateService.findByUserId(userId);

        model.addAttribute("entity", entity);
        model.addAttribute("emailSettings", emailSettings);
        model.addAttribute("namesAndFields", namesAndFields);
        model.addAttribute("emailTemplates", emailTemplates);
        model.addAttribute("isGoogleUser", isGoogleUser);
        model.addAttribute("gmailAccess", gmailAccess);

        return "settings/email";
    }

    @PostMapping("/email/{entity}")
    public String saveEmailSettings(RedirectAttributes redirectAttributes, @PathVariable("entity") String entity,
                                    @RequestParam Map<String, String> formParams, Authentication authentication) {

        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if(user.isInactiveUser()) {
            return "error/account-inactive";
        }
        switch (entity) {
            case "ticket" -> {
                TicketEmailSettings ticketEmailSettings = new TicketEmailSettings();
                if (!processEmailSettings(ticketEmailSettings, formParams)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Please select an email template before proceeding.");
                    return "redirect:/employee/settings/email/" + entity;
                }
                ticketEmailSettings.setUser(user);
                ticketEmailSettingsService.save(ticketEmailSettings);
            }
            case "lead" -> {
                LeadEmailSettings leadEmailSettings = new LeadEmailSettings();
                if (!processEmailSettings(leadEmailSettings, formParams)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Please select an email template before proceeding.");
                    return "redirect:/employee/settings/email/" + entity;
                }
                leadEmailSettings.setUser(user);
                leadEmailSettingsService.save(leadEmailSettings);
            }
            case "contract" -> {
                ContractEmailSettings contractEmailSettings = new ContractEmailSettings();
                if (!processEmailSettings(contractEmailSettings, formParams)) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Please select an email template before proceeding.");
                    return "redirect:/employee/settings/email/" + entity;
                }
                contractEmailSettings.setUser(user);
                contractEmailSettingsService.save(contractEmailSettings);
            }
            default -> {
                // Handle invalid entity case or return an error
                return "error/404";
            }
        }
        return "redirect:/employee/settings/email/" + entity;
    }

    private boolean processEmailSettings(EmailSettings emailSettings, Map<String, String> formParams) {
        Class<?> emailSettingsClass = emailSettings.getClass();

        for (Map.Entry<String, String> entry : formParams.entrySet()) {
            if (entry.getKey().startsWith("_") || entry.getKey().equals("csrf") || (entry.getKey().equals("id") && entry.getValue().isEmpty())) {
                continue;
            }

            String propertyName = StringUtils.replaceCharToCamelCase(entry.getKey(), '_');
            String setterMethodName = "set" + StringUtils.capitalizeFirstLetter(propertyName);

            try {
                Method setterMethod;
                if (entry.getKey().equals("id")) {
                    Integer num = Integer.parseInt(entry.getValue());
                    setterMethod = emailSettingsClass.getMethod(setterMethodName, num.getClass());
                    setterMethod.invoke(emailSettings, num);
                    continue;
                }
                if (entry.getKey().contains("template")) {
                    if (entry.getValue() == null || entry.getValue().isEmpty()) {
                        return false;
                    }
                    EmailTemplate emailTemplate = emailTemplateService.findByTemplateId(Integer.parseInt(entry.getValue()));
                    setterMethod = emailSettingsClass.getMethod(setterMethodName, emailTemplate.getClass());
                    setterMethod.invoke(emailSettings, emailTemplate);
                } else {
                    Boolean checked = Boolean.valueOf(entry.getValue());
                    setterMethod = emailSettingsClass.getMethod(setterMethodName, checked.getClass());
                    setterMethod.invoke(emailSettings, checked);
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                return false;
            }
        }
        return true;
    }

}
