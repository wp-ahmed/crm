package site.easy.to.build.crm.google.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.http.HttpResponseException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.model.gmail.Attachment;
import site.easy.to.build.crm.google.model.gmail.*;
import site.easy.to.build.crm.google.model.gmail.EmailPage;
import site.easy.to.build.crm.google.service.gmail.GmailEmailService;
import site.easy.to.build.crm.google.service.gmail.GoogleGmailApiService;
import site.easy.to.build.crm.google.service.gmail.GoogleGmailLabelService;
import site.easy.to.build.crm.util.AuthenticationUtils;
import site.easy.to.build.crm.google.util.PageTokenManager;
import site.easy.to.build.crm.util.SessionUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;
import java.util.*;

@Controller
@RequestMapping("/employee/gmail")
public class GoogleGmailController {
    private final AuthenticationUtils authenticationUtils;
    private final GmailEmailService gmailEmailService;

    private final GoogleGmailApiService googleGmailApiService;

    private final GoogleGmailLabelService googleGmailLabelService;

    @Autowired
    public GoogleGmailController(AuthenticationUtils authenticationUtils, GmailEmailService gmailEmailService, GoogleGmailApiService googleGmailApiService, GoogleGmailLabelService googleGmailLabelService) {
        this.authenticationUtils = authenticationUtils;
        this.gmailEmailService = gmailEmailService;
        this.googleGmailApiService = googleGmailApiService;
        this.googleGmailLabelService = googleGmailLabelService;
    }

    @GetMapping("/send")
    public String showEmailForm(Model model, Authentication authentication) {
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        if(!oAuthUser.getGrantedScopes().contains("https://www.googleapis.com/auth/gmail.modify")){
            String link = "employee/settings/google-services";
            String code = "403";
            String buttonText = "Grant Access";
            String message = "Please grant the app access to Gmail  in order to use this service";
            model.addAttribute("link",link);
            model.addAttribute("message",message);
            model.addAttribute("buttonText",buttonText);
            model.addAttribute("code",code);
            return "gmail/error";
        }
        model.addAttribute("emailForm", new GmailEmailInfo());
        return "gmail/email-form";
    }

    @GetMapping("/send-draft/{draftId}")
    public String showEmailFormOfDraft(Model model, @PathVariable("draftId") String draftId, Authentication authentication) {
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        GmailEmailInfo emailForm;
        try {
            emailForm = googleGmailApiService.getDraft(oAuthUser,draftId);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
        model.addAttribute("emailForm", emailForm);
        return "gmail/email-form";
    }
    @PostMapping("/upload")
    public ResponseEntity<Void> uploadAttachment() {
        // Simulate a successful file upload by returning a 200 OK response
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send")
    public String sendEmail(Authentication authentication, @RequestParam("allFiles") String files,
                            @ModelAttribute("emailForm") @Valid GmailEmailInfo emailForm,
                            BindingResult bindingResult) {
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        if (bindingResult.hasErrors()) {
            return "gmail/email-form";
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Attachment> allFiles = objectMapper.readValue(files, new TypeReference<List<Attachment>>(){});
            googleGmailApiService.sendEmail(oAuthUser, emailForm.getRecipient(), emailForm.getSubject(), emailForm.getBody(), new ArrayList<>(),allFiles);
            googleGmailApiService.removeDraft(oAuthUser,emailForm.getDraftId());

        }catch (SocketTimeoutException e) {
            bindingResult.rejectValue("failedErrorMessage", "error.failedErrorMessage","There are might be a network connection error, please check your connection and try again!");
            return "gmail/email-form";
        } catch (Exception e) {
            ObjectError emailFailingError = new ObjectError("failedErrorMessage", "The email hasn't been sent, please try again!");
            bindingResult.addError(emailFailingError);
            return "gmail/email-form";
        }
        return "redirect:/employee/gmail/emails/sent?success=true";
    }

    @PostMapping("/draft/ajax")
    @ResponseBody
    public ResponseEntity<String> saveDraftAjax(Authentication authentication, @ModelAttribute("emailForm") GmailEmailInfo emailForm,
                                                BindingResult bindingResult, HttpSession session,
                                                @RequestParam("files") String files) throws JsonProcessingException {
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        ObjectMapper objectMapper = new ObjectMapper();
        List<Attachment> allFiles = objectMapper.readValue(files, new TypeReference<List<Attachment>>(){});

        try {
            if (emailForm.getDraftId() != null && !emailForm.getDraftId().isEmpty()) {
                googleGmailApiService.updateDraft(oAuthUser, emailForm.getDraftId(), emailForm.getRecipient(), emailForm.getSubject(), emailForm.getBody(), new ArrayList<>(), allFiles);
            } else {
                String draftId = googleGmailApiService.createDraft(oAuthUser, emailForm.getRecipient(), emailForm.getSubject(), emailForm.getBody(), new ArrayList<>(),allFiles);
                emailForm.setDraftId(draftId);
            }
            return ResponseEntity.ok(emailForm.getDraftId());
        } catch (IOException | GeneralSecurityException | MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving draft: " + e.getMessage());
        }
    }
    @GetMapping("/emails")
    public String showEmails(HttpSession session, Authentication authentication, Model model,
                             @RequestParam(value = "page", defaultValue = "1") int page) throws IOException {
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }

        EmailPage emailsPerPage;
        List<String> labels = null;
        int count;
        int draft;
        try {
            emailsPerPage = getEmailsByLabel(session, authentication, page, "inbox");
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
//            labels = googleGmailLabelService.fetchAllLabels(oAuthUser);
            count = googleGmailApiService.getEmailsCount(oAuthUser,"in:inbox category:primary is:unread");
            draft = googleGmailApiService.getEmailsCount(oAuthUser,"is:draft");
            googleGmailApiService.getEmailsCount(oAuthUser, "in:inbox category:primary is:unread");
        }catch (GeneralSecurityException | IOException e) {
            String link = "";
            String code = "400";
            String buttonText = "Go Home";
            String message = "There was a problem retrieving the emails now, Please try again later!";
            int prevPage = page;
            if (e instanceof HttpResponseException httpResponseException) {
                int statusCode = httpResponseException.getStatusCode();
                if(statusCode == 403){
                    code = "403";
                    link = "employee/settings/google-services";
                    buttonText = "Grant Access";
                    message = "Please grant the app access to Gmail  in order to use this service";
                }
            }else if(page>1){
                prevPage--;
                link = "employee/gmail/emails?page="+prevPage;
                buttonText = "GO Back";
                message = "There was a problem retrieving the emails at this page, Please try again later!";
            }

            model.addAttribute("link",link);
            model.addAttribute("message",message);
            model.addAttribute("buttonText",buttonText);
            model.addAttribute("code",code);
            return "gmail/error";
        }

        model.addAttribute("emails", emailsPerPage.getEmails());
        model.addAttribute("nextToken", emailsPerPage.getNextPageToken());
        model.addAttribute("labels", "inbox");
        model.addAttribute("count", count);
        model.addAttribute("draft",draft);

        addPaginationAttributes(model, page);
        return "gmail/emails";
    }

    @GetMapping("/emails-json")
    public @ResponseBody
    EmailPage getEmailsJson(HttpSession session, Authentication authentication,
                            @RequestParam(value = "page", defaultValue = "1") int page)
            throws GeneralSecurityException, IOException {
        if(page<1){
            page = 1;
        }
        return getEmailsByLabel(session, authentication, page, "inbox");
    }

    @GetMapping("/emails/{label}")
    public String showSentEmails(HttpServletRequest request, HttpSession session, Authentication authentication, Model model,
                                 @PathVariable("label") String label,
                                 @RequestParam(value = "page", defaultValue = "1") int page, @RequestParam(value = "success", required = false) boolean success) {
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }
        if (success) {
            model.addAttribute("successMessage", "Email sent successfully!");
        }
        EmailPage emailsPerPage;
        int count;
        int draft;
        try {
            OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
            emailsPerPage = getEmailsByLabel(session, authentication, page, label);
            count = googleGmailApiService.getEmailsCount(oAuthUser,"in:inbox category:primary is:unread");
            draft = googleGmailApiService.getEmailsCount(oAuthUser,"is:draft");
        }catch (GeneralSecurityException | IOException e) {
            int prevPage = page;
            String link = "";
            String buttonText = "Go Home";
            String message = "There was a problem retrieving the emails now, Please try again later!";
            String code = "400";
            if (e instanceof HttpResponseException httpResponseException) {
                int statusCode = httpResponseException.getStatusCode();
                if(statusCode == 403){
                    code = "403";
                    link = "employee/settings/google-services";
                    buttonText = "Grant Access";
                    message = "Please grant the app access to Gmail  in order to use this service";
                }
            }else if(page>1){
                prevPage--;
                link = "employee/gmail/emails/" + label + "?page=" + prevPage;
                buttonText = "GO Back";
                message = "There was a problem retrieving the emails at this page, Please try again later!";
            }

            model.addAttribute("link",link);
            model.addAttribute("message",message);
            model.addAttribute("buttonText",buttonText);
            model.addAttribute("code",code);
            return "gmail/error";
        }
        model.addAttribute("emails", emailsPerPage.getEmails());
        model.addAttribute("count", count);
        model.addAttribute("draft", draft);
        model.addAttribute("label", label);
        addPaginationAttributes(model, page);
        return "gmail/emails-label";
    }
    @GetMapping("/emails-json/{label}")
    public @ResponseBody
    EmailPage getSentEmailsJson(HttpSession session, Authentication authentication,
                                @PathVariable("label") String label,
                                @RequestParam(value = "page", defaultValue = "1") int page){
        try {
            return getEmailsByLabel(session, authentication, page, label);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private EmailPage getEmailsByLabel(HttpSession session, Authentication authentication, int page, String label)
            throws GeneralSecurityException, IOException {
        int maxResult = 10;
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);

        // Retrieve or create a new PageTokenManager
        PageTokenManager pageTokenManager = Optional.ofNullable(SessionUtils.getSessionAttribute(session, "pageTokenManager", PageTokenManager.class))
                .orElseGet(PageTokenManager::new);

        String pageToken = null;
        if (page != 1) {
            pageToken = gmailEmailService.getPageTokenForPage(pageTokenManager, page, oAuthUser, maxResult, label);
        }
        EmailPage emailsPerPage = gmailEmailService.getEmailsPerPage(oAuthUser, maxResult, pageToken, label);

        // Update the PageTokenManager with the nextPageToken
        Optional.ofNullable(emailsPerPage.getNextPageToken())
                .ifPresent(nextPageToken -> {
                    pageTokenManager.setPageToken(page + 1, nextPageToken);
                    session.setAttribute("pageTokenManager", pageTokenManager);
                });
        emailsPerPage.setPage(page);
        return emailsPerPage;
    }

    private void addPaginationAttributes(Model model, int currentPage) {
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("nextPage", currentPage + 1);
        model.addAttribute("prevPage", currentPage - 1);
    }
    @GetMapping("/email-details/{id}")
    public String showEmailDetails(@PathVariable("id") String emailId, Authentication authentication, Model model, HttpSession session) {
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }

        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        GmailEmailInfo emailInfo;
        int count;
        int draft;
        try {
            count = googleGmailApiService.getEmailsCount(oAuthUser,"in:inbox category:primary is:unread");
            draft = googleGmailApiService.getEmailsCount(oAuthUser,"is:draft");
            emailInfo = googleGmailApiService.getEmailDetails(oAuthUser,emailId);
            googleGmailApiService.updateEmail(oAuthUser,emailId);

        } catch (GeneralSecurityException | IOException e) {
            String link = "";
            String buttonText = "Go Home";
            String message = "There was a problem retrieving the email now, Please try again later!";
            String code = "400";
            if (e instanceof HttpResponseException httpResponseException) {
                int statusCode = httpResponseException.getStatusCode();
                if(statusCode == 403){
                    code = "403";
                    link = "employee/settings/google-services";
                    buttonText = "Grant Access";
                    message = "Please grant the app access to Gmail  in order to use this service";
                }
            }

            model.addAttribute("link",link);
            model.addAttribute("message",message);
            model.addAttribute("buttonText",buttonText);
            model.addAttribute("code",code);
            return "gmail/error";
        }
        model.addAttribute("emailInfo",emailInfo);
        model.addAttribute("count", count);
        model.addAttribute("draft", draft);
        return "gmail/email-details";
    }
    @PostMapping("/deleteEmail")
    public String deleteEmail(Authentication authentication,
                              @RequestParam("emailId") String emailId,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              RedirectAttributes redirectAttributes) {
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);

        gmailEmailService.deleteEmail(oAuthUser, emailId, redirectAttributes);

        return "redirect:/employee/gmail/emails?page=" + page;
    }
}
