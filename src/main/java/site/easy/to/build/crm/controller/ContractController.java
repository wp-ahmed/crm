package site.easy.to.build.crm.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import site.easy.to.build.crm.cron.ContractExpirationChecker;
import site.easy.to.build.crm.entity.*;
import site.easy.to.build.crm.entity.settings.ContractEmailSettings;
import site.easy.to.build.crm.google.model.drive.GoogleDriveFolder;
import site.easy.to.build.crm.google.model.gmail.Attachment;
import site.easy.to.build.crm.google.service.acess.GoogleAccessService;
import site.easy.to.build.crm.google.service.drive.GoogleDriveApiService;
import site.easy.to.build.crm.google.service.gmail.GoogleGmailApiService;
import site.easy.to.build.crm.service.contract.ContractService;
import site.easy.to.build.crm.service.customer.CustomerService;
import site.easy.to.build.crm.service.drive.GoogleDriveFileService;
import site.easy.to.build.crm.service.file.FileService;
import site.easy.to.build.crm.service.lead.LeadService;
import site.easy.to.build.crm.service.settings.ContractEmailSettingsService;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/employee/contract")
public class ContractController {

    private final ContractService contractService;

    private final AuthenticationUtils authenticationUtils;

    private final UserService userService;

    private final CustomerService customerService;

    private final LeadService leadService;

    private final GoogleDriveApiService googleDriveApiService;
    private final FileUtil fileUtil;
    private final FileService fileService;
    private final GoogleDriveFileService googleDriveFileService;
    private final ContractExpirationChecker contractExpirationChecker;
    private final EntityManager entityManager;
    private final ContractEmailSettingsService contractEmailSettingsService;
    private final GoogleGmailApiService googleGmailApiService;

    @Autowired
    public ContractController(ContractService contractService, AuthenticationUtils authenticationUtils, UserService userService,
                              CustomerService customerService, LeadService leadService, GoogleDriveApiService googleDriveApiService,
                              FileUtil fileUtil, FileService fileService, GoogleDriveFileService googleDriveFileService,
                              ContractExpirationChecker contractExpirationChecker, EntityManager entityManager,
                              ContractEmailSettingsService contractEmailSettingsService,
                              GoogleGmailApiService googleGmailApiService) {
        this.contractService = contractService;
        this.authenticationUtils = authenticationUtils;
        this.userService = userService;
        this.customerService = customerService;
        this.leadService = leadService;
        this.googleDriveApiService = googleDriveApiService;
        this.fileUtil = fileUtil;
        this.fileService = fileService;
        this.googleDriveFileService = googleDriveFileService;
        this.contractExpirationChecker = contractExpirationChecker;
        this.entityManager = entityManager;
        this.contractEmailSettingsService = contractEmailSettingsService;
        this.googleGmailApiService = googleGmailApiService;
    }

    @GetMapping("/show-details/{id}")
    public String showContractDetails(@PathVariable("id") int id, Model model, Authentication authentication) {
        Contract contract = contractService.findByContractId(id);
        if (contract == null) {
            return "error/not-found";
        }
        User employee = contract.getUser();
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if(loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        if (!AuthorizationUtil.checkIfUserAuthorized(employee, loggedInUser) && !AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            return "error/access-denied";
        }

        contractExpirationChecker.scheduleContractExpirationCheck(contract);
        List<File> files = contract.getFiles();
        List<Attachment> attachments = new ArrayList<>();
        for (File file : files) {
            String base64Data = Base64.getEncoder().encodeToString(file.getFileData());
            Attachment attachment = new Attachment(file.getFileName(), base64Data, file.getFileType());
            attachments.add(attachment);
        }
        model.addAttribute("contract", contract);
        model.addAttribute("attachments", attachments);
        return "contract/show-details";
    }

    @GetMapping("/manager/show-all")
    public String getAllContracts(Model model) {
        List<Contract> contracts = contractService.findAll();
        model.addAttribute("contracts", contracts);
        return "contract/contracts";
    }

    @GetMapping("/my-contracts")
    public String getEmployeeContracts(Model model, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        List<Contract> contracts = contractService.getEmployeeCreatedContracts(userId);
        model.addAttribute("contracts", contracts);
        return "contract/contracts";
    }

    @GetMapping("/customer-contracts/{customerId}")
    public String getCustomerContracts(Model model, @PathVariable("customerId") int customerId, Authentication authentication) {
        List<Contract> contracts = contractService.getCustomerContracts(customerId);
        //TODO after activate the login by customer do the authorization check
        model.addAttribute("contracts", contracts);
        return "contract/contracts";
    }

    @GetMapping("/create")
    public String showCreatingForm(Model model, @RequestParam(value = "leadId", required = false) Integer leadId, Authentication authentication) {

        Lead lead = (leadId != null) ? leadService.findByLeadId(leadId) : null;
        List<Attachment> attachments = new ArrayList<>();

        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User user = userService.findById(userId);
        if (user.isInactiveUser()) {
            return "error/account-inactive";
        }

        List<Customer> customers;

        if (AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            customers = customerService.findAll();
        } else {
            customers = customerService.findByUserId(userId);
        }

        List<GoogleDriveFolder> folders = null;
        boolean hasGoogleDriveAccess = false;
        if (!(authentication instanceof UsernamePasswordAuthenticationToken) && googleDriveApiService != null) {
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            try {
                hasGoogleDriveAccess = authenticationUtils.checkIfAppHasAccess(GoogleAccessService.SCOPE_DRIVE, oAuthUser);
                if (hasGoogleDriveAccess) {
                    folders = googleDriveApiService.listFolders(oAuthUser);
                }
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }

        model.addAttribute("customers", customers);
        model.addAttribute("lead", lead);
        model.addAttribute("folders", folders);
        model.addAttribute("attachments", attachments);
        model.addAttribute("hasGoogleDriveAccess", hasGoogleDriveAccess);
        model.addAttribute("contract", new Contract());

        return "contract/create";
    }

    @PostMapping("/create")
    public String createNewContract(@ModelAttribute("contract") @Validated Contract contract, BindingResult bindingResult, @RequestParam("customerId") int customerId,
                                    @RequestParam("leadId") @Nullable Integer leadId, Authentication authentication, Model model,
                                    @RequestParam("allFiles") @Nullable String files, @RequestParam("folderId") @Nullable String folderId)
            throws IOException, GeneralSecurityException {

        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User employee = userService.findById(userId);
        if (employee.isInactiveUser()) {
            return "error/account-inactive";
        }
        Customer customer = customerService.findByCustomerId(customerId);

        if (customer == null || customer.getUser().getId() != userId && !AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            return "error/500";
        }

        Lead lead = (leadId != null) ? leadService.findByLeadId(leadId) : null;
        if (lead != null) {
            contract.setLead(lead);
        }

        if (bindingResult.hasErrors()) {
            List<Customer> customers;
            List<Attachment> attachments = new ArrayList<>();
            if (AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
                customers = customerService.findAll();
            } else {
                customers = customerService.findByUserId(userId);
            }

            List<GoogleDriveFolder> folders = null;
            boolean hasGoogleDriveAccess = false;
            if (!(authentication instanceof UsernamePasswordAuthenticationToken) && googleDriveApiService != null) {
                OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
                try {
                    hasGoogleDriveAccess = authenticationUtils.checkIfAppHasAccess(GoogleAccessService.SCOPE_DRIVE, oAuthUser);
                    if (hasGoogleDriveAccess) {
                        folders = googleDriveApiService.listFolders(oAuthUser);
                    }
                } catch (IOException | GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            }

            model.addAttribute("customers", customers);
            model.addAttribute("lead", lead);
            model.addAttribute("folders", folders);
            model.addAttribute("attachments", attachments);
            model.addAttribute("hasGoogleDriveAccess", hasGoogleDriveAccess);
            return "contract/create";
        }
        contract.setCustomer(customer);
        contract.setUser(employee);
        contract.setGoogleDriveFolderId(folderId);
        contract.setCreatedAt(LocalDateTime.now());

        ObjectMapper objectMapper = new ObjectMapper();
        List<Attachment> allFiles = objectMapper.readValue(files, new TypeReference<List<Attachment>>() {
        });

        if (!(authentication instanceof UsernamePasswordAuthenticationToken) && googleDriveApiService != null) {
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            try {
                if (folderId != null && !folderId.isEmpty()) {
                    googleDriveApiService.checkFolderExists(oAuthUser, folderId);
                }
            } catch (IOException | GeneralSecurityException e) {
                return "error/500";
            }
        }

        Contract createdContract = contractService.save(contract);
        fileUtil.saveFiles(allFiles, createdContract);

        if (contract.getGoogleDrive() != null) {
            fileUtil.saveGoogleDriveFiles(authentication, allFiles, folderId, createdContract);
        }

        return "redirect:/employee/contract/my-contracts";
    }

    @GetMapping("/update/{id}")
    public String showUpdateForm(Model model, Authentication authentication, @PathVariable("id") int id) {
        Contract contract = contractService.findByContractId(id);
        if (contract == null) {
            return "error/not-found";
        }
        User employee = contract.getUser();
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if (loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }

        if (!AuthorizationUtil.checkIfUserAuthorized(employee, loggedInUser) && !AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            return "error/access-denied";
        }

        List<Customer> customers;
        if (AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
            customers = customerService.findAll();
        } else {
            customers = customerService.findByUserId(userId);
        }

        List<File> files = contract.getFiles();

        List<Attachment> attachments = new ArrayList<>();
        for (File file : files) {
            String base64Data = Base64.getEncoder().encodeToString(file.getFileData());
            Attachment attachment = new Attachment(file.getFileName(), base64Data, file.getFileType());
            attachments.add(attachment);
        }

        List<GoogleDriveFolder> folders = null;

        boolean hasGoogleDriveAccess = false;
        if (!(authentication instanceof UsernamePasswordAuthenticationToken) && googleDriveApiService != null) {
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            List<GoogleDriveFile> googleDriveFiles = contract.getGoogleDriveFiles();
            try {
                hasGoogleDriveAccess = authenticationUtils.checkIfAppHasAccess(GoogleAccessService.SCOPE_DRIVE, oAuthUser);
                if (hasGoogleDriveAccess) {
                    folders = googleDriveApiService.listFolders(oAuthUser);
                }

                fileUtil.updateFilesBasedOnGoogleDriveFiles(oAuthUser, googleDriveFiles, contract);

            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }
        }

        model.addAttribute("customers", customers);
        model.addAttribute("folders", folders);
        model.addAttribute("attachments", attachments);
        model.addAttribute("hasGoogleDriveAccess", hasGoogleDriveAccess);
        model.addAttribute("contract", contract);

        return "contract/update";
    }

    @PostMapping("/update")
    public String updateContract(@ModelAttribute("contract") @Validated Contract contract, BindingResult bindingResult,
                                 Authentication authentication, @RequestParam("customerId") int customerId, Model model,
                                 @RequestParam("allFiles") @Nullable String files, @RequestParam("folderId") @Nullable String folderId) throws JsonProcessingException {

        Contract prevContract = contractService.findByContractId(contract.getContractId());
        if (prevContract == null) {
            return "error/500";
        }
        Contract originalContract = new Contract();
        BeanUtils.copyProperties(prevContract, originalContract);

        int userId = authenticationUtils.getLoggedInUserId(authentication);

        Customer customer = customerService.findByCustomerId(customerId);
        User employee = userService.findById(userId);
        if (employee.isInactiveUser()) {
            return "error/account-inactive";
        }
        if (customer == null || (customer.getUser().getId() != userId && !AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER"))) {
            return "error/500";
        }

        if (bindingResult.hasErrors()) {
            List<Customer> customers;
            if (AuthorizationUtil.hasRole(authentication, "ROLE_MANAGER")) {
                customers = customerService.findAll();
            } else {
                customers = customerService.findByUserId(userId);
            }

            List<File> tempFiles = originalContract.getFiles();

            List<Attachment> attachments = new ArrayList<>();
            for (File file : tempFiles) {
                String base64Data = Base64.getEncoder().encodeToString(file.getFileData());
                Attachment attachment = new Attachment(file.getFileName(), base64Data, file.getFileType());
                attachments.add(attachment);
            }

            List<GoogleDriveFolder> folders = null;

            boolean hasGoogleDriveAccess = false;
            if (!(authentication instanceof UsernamePasswordAuthenticationToken) && googleDriveApiService != null) {
                OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
                List<GoogleDriveFile> googleDriveFiles = originalContract.getGoogleDriveFiles();
                try {
                    hasGoogleDriveAccess = authenticationUtils.checkIfAppHasAccess(GoogleAccessService.SCOPE_DRIVE, oAuthUser);
                    if (hasGoogleDriveAccess) {
                        folders = googleDriveApiService.listFolders(oAuthUser);
                    }

                    fileUtil.updateFilesBasedOnGoogleDriveFiles(oAuthUser, googleDriveFiles, originalContract);

                } catch (IOException | GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            }

            model.addAttribute("customers", customers);
            model.addAttribute("folders", folders);
            model.addAttribute("attachments", attachments);
            model.addAttribute("hasGoogleDriveAccess", hasGoogleDriveAccess);
            return "contract/update";
        }

        contract.setCustomer(customer);
        contract.setGoogleDriveFolderId(folderId);
        contract.setCreatedAt(originalContract.getCreatedAt());

        List<File> oldFiles = fileService.getContractFiles(contract.getContractId());
        List<GoogleDriveFile> oldGoogleDriveFiles = googleDriveFileService.getAllDriveFileByContactId(contract.getContractId());

        ObjectMapper objectMapper = new ObjectMapper();
        List<Attachment> allFiles = objectMapper.readValue(files, new TypeReference<List<Attachment>>() {
        });

        contract.setUser(employee);
        contract.setCustomer(customer);
        contract.setGoogleDriveFolderId(folderId);

        fileUtil.deleteOldFiles(oldFiles, contract);
        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            fileUtil.deleteGoogleDriveFiles(oldGoogleDriveFiles, authentication);
        }

        fileUtil.saveFiles(allFiles, contract);
        if (!(authentication instanceof UsernamePasswordAuthenticationToken) && contract.getGoogleDrive() && googleDriveApiService != null) {
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            try {
                if (folderId != null && !folderId.isEmpty()) {
                    googleDriveApiService.checkFolderExists(oAuthUser, folderId);
                }
            } catch (IOException | GeneralSecurityException e) {
                return "error/500";
            }
            fileUtil.saveGoogleDriveFiles(authentication, allFiles, folderId, contract);
        }


        Contract currentContract = contractService.save(contract);
        List<String> properties = DatabaseUtil.getColumnNames(entityManager, Contract.class);
        Map<String, Pair<String, String>> changes = LogEntityChanges.trackChanges(originalContract, currentContract, properties);

        boolean isGoogleUser = !(authentication instanceof UsernamePasswordAuthenticationToken);

        if (isGoogleUser && googleGmailApiService != null) {
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            if (oAuthUser.getGrantedScopes().contains(GoogleAccessService.SCOPE_GMAIL)) {
                try {
                    processEmailSettingsChanges(changes, userId, oAuthUser, customer);
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return "redirect:/employee/contract/my-contracts";
    }

    @PostMapping("/delete/{id}")
    public String deleteLead(@PathVariable("id") int id, Authentication authentication) {
        Contract contract = contractService.findByContractId(id);

        User employee = contract.getUser();
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        User loggedInUser = userService.findById(userId);
        if (loggedInUser.isInactiveUser()) {
            return "error/account-inactive";
        }
        if (!AuthorizationUtil.checkIfUserAuthorized(employee, loggedInUser)) {
            return "error/access-denied";
        }

        contractService.delete(contract);
        return "redirect:/employee/contract/my-contracts";
    }

    private void processEmailSettingsChanges(Map<String, Pair<String, String>> changes, int userId, OAuthUser oAuthUser,
                                             Customer customer) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        for (Map.Entry<String, Pair<String, String>> entry : changes.entrySet()) {
            String property = entry.getKey();
            String propertyName = StringUtils.replaceCharToCamelCase(property, '_');
            propertyName = StringUtils.replaceCharToCamelCase(propertyName, ' ');

            String prevState = entry.getValue().getFirst();
            String nextState = entry.getValue().getSecond();

            ContractEmailSettings contractEmailSettings = contractEmailSettingsService.findByUserId(userId);
            CustomerLoginInfo customerLoginInfo = customer.getCustomerLoginInfo();
            ContractEmailSettings customerContractEmailSettings = contractEmailSettingsService.findByCustomerId(customerLoginInfo.getId());
            if (contractEmailSettings != null) {
                String getterMethodName = "get" + StringUtils.capitalizeFirstLetter(propertyName);
                Method getterMethod = ContractEmailSettings.class.getMethod(getterMethodName);
                Boolean propertyValue = (Boolean) getterMethod.invoke(contractEmailSettings);

                Boolean isCustomerLikeToGetNotified = true;
                if (customerContractEmailSettings != null) {
                    isCustomerLikeToGetNotified = (Boolean) getterMethod.invoke(customerContractEmailSettings);
                }

                if (isCustomerLikeToGetNotified != null && propertyValue != null && propertyValue && isCustomerLikeToGetNotified) {
                    String emailTemplateGetterMethodName = "get" + StringUtils.capitalizeFirstLetter(propertyName) + "EmailTemplate";

                    Method emailTemplateGetterMethod = ContractEmailSettings.class.getMethod(emailTemplateGetterMethodName);
                    EmailTemplate emailTemplate = (EmailTemplate) emailTemplateGetterMethod.invoke(contractEmailSettings);
                    String body = emailTemplate.getContent();

                    property = property.replace(' ', '_');
                    String regex = "\\{\\{(.*?)\\}\\}";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(body);

                    while (matcher.find()) {
                        String placeholder = matcher.group(1);
                        if (placeholder.contains("previous") && placeholder.contains(property)) {
                            body = body.replace("{{" + placeholder + "}}", prevState);
                        } else if (placeholder.contains("next") && placeholder.contains(property)) {
                            body = body.replace("{{" + placeholder + "}}", nextState);
                        }
                    }

                    try {
                        googleGmailApiService.sendEmail(oAuthUser, customer.getEmail(), emailTemplate.getName(), body);
                    } catch (IOException | GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }
}
