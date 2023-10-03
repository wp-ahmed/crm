package site.easy.to.build.crm.controller;

import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.entity.settings.ContractEmailSettings;
import site.easy.to.build.crm.entity.settings.EmailSettings;
import site.easy.to.build.crm.entity.settings.LeadEmailSettings;
import site.easy.to.build.crm.entity.settings.TicketEmailSettings;
import site.easy.to.build.crm.service.email.EmailTemplateService;
import site.easy.to.build.crm.service.settings.ContractEmailSettingsService;
import site.easy.to.build.crm.service.settings.LeadEmailSettingsService;
import site.easy.to.build.crm.service.settings.TicketEmailSettingsService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.util.AuthorizationUtil;
import site.easy.to.build.crm.util.DatabaseUtil;
import site.easy.to.build.crm.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/employee/email-template")
public class EmailTemplatesController {

    private final EntityManager entityManager;
    private final EmailTemplateService emailTemplateService;
    private final AuthenticationUtils authenticationUtils;
    private final UserService userService;
    private final ContractEmailSettingsService contractEmailSettingsService;
    private final TicketEmailSettingsService ticketEmailSettingsService;
    private final LeadEmailSettingsService leadEmailSettingsService;

    public EmailTemplatesController(EntityManager entityManager, EmailTemplateService emailTemplateService, AuthenticationUtils authenticationUtils,
                                    UserService userService, ContractEmailSettingsService contractEmailSettingsService, TicketEmailSettingsService ticketEmailSettingsService, LeadEmailSettingsService leadEmailSettingsService) {
        this.entityManager = entityManager;
        this.emailTemplateService = emailTemplateService;
        this.authenticationUtils = authenticationUtils;
        this.userService = userService;
        this.contractEmailSettingsService = contractEmailSettingsService;
        this.ticketEmailSettingsService = ticketEmailSettingsService;
        this.leadEmailSettingsService = leadEmailSettingsService;
    }

    @GetMapping("/show/{id}")
    public String showTemplate(Model model, @PathVariable("id") int id, Authentication authentication) {
        EmailTemplate emailTemplate = emailTemplateService.findByTemplateId(id);

        User employee = emailTemplate.getUser();
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if (loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        if (!AuthorizationUtil.checkIfUserAuthorized(employee, loggedInUser) && !AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            return "error/access-denied";
        }

        model.addAttribute("emailTemplate", emailTemplate);
        return "email-template/show-template";
    }

    @GetMapping("/manager/show-all")
    public String showAllTemplates(Model model) {
        List<EmailTemplate> emailTemplates = emailTemplateService.getAllTemplates();
        model.addAttribute("emailTemplates", emailTemplates);
        return "email-template/show-all";
    }

    @GetMapping("/my-templates")
    public String showMyTemplates(Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        List<EmailTemplate> emailTemplates = emailTemplateService.findByUserId(userId);
        model.addAttribute("emailTemplates", emailTemplates);
        return "email-template/show-all";
    }

    @GetMapping("/create")
    public String showEmailTemplate(Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if (loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        List<String> tags = new ArrayList<>();
        Set<Class<?>> classes = DatabaseUtil.getAllEntitiesWithTriggerTable(entityManager);
        for (Class<?> className : classes) {
            String name = className.getSimpleName();
            List<String> properties = DatabaseUtil.getColumnNames(entityManager, className);
            for (String property : properties) {
                property = property.replace(' ', '_');
                String prevState = name + "_" + property + "_previous_state";
                String nextState = name + "_" + property + "_next_state";
                tags.add(prevState);
                tags.add(name);
                tags.add(nextState);
            }
        }

        model.addAttribute("tags", tags);
        return "email-template/create-template";
    }

    @PostMapping("/create")
    public ResponseEntity<String> createEmailTemplate(@RequestBody Map<String, String> requestPayload, Authentication authentication,
                                                      RedirectAttributes redirectAttributes) {
        String name = requestPayload.get("name");
        EmailTemplate currentEmailTemplate = emailTemplateService.findByName(name);
        if (currentEmailTemplate != null) {
            String errorMessage = "The name is not unique.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
        String content = requestPayload.get("content");
        String jsonDesign = requestPayload.get("jsonDesign");

        EmailTemplate emailTemplate = new EmailTemplate();
        emailTemplate.setContent(content);
        emailTemplate.setName(name);
        emailTemplate.setJsonDesign(jsonDesign);
        emailTemplate.setCreatedAt(LocalDateTime.now());
        emailTemplateService.save(emailTemplate, authentication);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/update/{id}")
    public String showUpdateEmailForm(@PathVariable("id") int id, Model model, Authentication authentication) {
        EmailTemplate emailTemplate = emailTemplateService.findByTemplateId(id);

        User employee = emailTemplate.getUser();
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if (loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        if (!AuthorizationUtil.checkIfUserAuthorized(employee, loggedInUser)) {
            return "error/access-denied";
        }

        List<String> tags = new ArrayList<>();
        Set<Class<?>> classes = DatabaseUtil.getAllEntitiesWithTriggerTable(entityManager);
        for (Class<?> className : classes) {
            String name = className.getSimpleName();
            List<String> properties = DatabaseUtil.getColumnNames(entityManager, className);
            for (String property : properties) {
                property = property.replace(' ', '_');
                String prevState = name + "_" + property + "_previous_state";
                String nextState = name + "_" + property + "_next_state";
                tags.add(prevState);
                tags.add(nextState);
            }
        }

        model.addAttribute("tags", tags);
        model.addAttribute("emailTemplate", emailTemplate);
        return "email-template/update-template";
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateEmailTemplate(@RequestBody Map<String, String> requestPayload,
                                                      Authentication authentication) {

        int id = Integer.parseInt(requestPayload.get("id"));
        EmailTemplate emailTemplate = emailTemplateService.findByTemplateId(id);
        String name = requestPayload.get("name");
        if (!name.equals(emailTemplate.getName()) && emailTemplateService.findByName(name) != null) {
            String errorMessage = "The name is not unique.";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
        }
        String content = requestPayload.get("content");
        String jsonDesign = requestPayload.get("jsonDesign");

        emailTemplate.setContent(content);
        emailTemplate.setName(name);
        emailTemplate.setJsonDesign(jsonDesign);
        emailTemplateService.save(emailTemplate, authentication);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/delete/{id}")
    public String deleteEmailTemplate(@PathVariable("id") int id, Authentication authentication) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        EmailTemplate emailTemplate = emailTemplateService.findByTemplateId(id);

        User employee = emailTemplate.getUser();
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if (loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }

        if (!AuthorizationUtil.checkIfUserAuthorized(employee, loggedInUser)) {
            return "error/access-denied";
        }

        ContractEmailSettings contractEmailSettings = contractEmailSettingsService.findByUserId(userId);
        TicketEmailSettings ticketEmailSettings = ticketEmailSettingsService.findByUserId(userId);
        LeadEmailSettings leadEmailSettings = leadEmailSettingsService.findByUserId(userId);

        deleteEmailTemplateAssociatedWithEntity(id, contractEmailSettings, Contract.class);
        deleteEmailTemplateAssociatedWithEntity(id, ticketEmailSettings, Ticket.class);
        deleteEmailTemplateAssociatedWithEntity(id, leadEmailSettings, Lead.class);

        emailTemplateService.delete(id);
        return "redirect:/employee/email-template/my-templates";
    }

    private <T> void deleteEmailTemplateAssociatedWithEntity(int templateId, EmailSettings emailSettings, Class<T> tClass)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Class<?> emailSettingsClass = emailSettings.getClass();
        List<String> columnNames = DatabaseUtil.getColumnNames(entityManager, tClass);
        for (String columnName : columnNames) {
            String getterMethod = StringUtils.replaceCharToCamelCase(columnName, ' ') + "EmailTemplate";
            getterMethod = "get" + StringUtils.capitalizeFirstLetter(getterMethod);
            Method emailTemplateGetterMethod = emailSettingsClass.getMethod(getterMethod);
            EmailTemplate emailTemplate = (EmailTemplate) emailTemplateGetterMethod.invoke(emailSettings);
            if (emailTemplate == null) {
                continue;
            }
            if (templateId == emailTemplate.getTemplateId()) {
                Boolean checked = false;
                String booleanSetterMethodName = StringUtils.replaceCharToCamelCase(columnName, ' ');
                booleanSetterMethodName = "set" + StringUtils.capitalizeFirstLetter(booleanSetterMethodName);
                Method booleanSetterMethod = emailSettingsClass.getMethod(booleanSetterMethodName, checked.getClass());
                booleanSetterMethod.invoke(emailSettings, checked);


                String emailTemplateSetterMethodName = StringUtils.replaceCharToCamelCase(columnName, ' ') + "EmailTemplate";
                emailTemplateSetterMethodName = "set" + StringUtils.capitalizeFirstLetter(emailTemplateSetterMethodName);
                Method setterMethod = emailSettingsClass.getMethod(emailTemplateSetterMethodName, EmailTemplate.class);
                setterMethod.invoke(emailSettings, (Object) null);
            }
        }
        if (tClass == Contract.class) {
            contractEmailSettingsService.save((ContractEmailSettings) emailSettings);
        } else if (tClass == Ticket.class) {
            ticketEmailSettingsService.save((TicketEmailSettings) emailSettings);
        } else if (tClass == Lead.class) {
            leadEmailSettingsService.save((LeadEmailSettings) emailSettings);
        }
    }
}
