package site.easy.to.build.crm.google.service.gmail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.model.gmail.EmailPage;
import site.easy.to.build.crm.google.util.PageTokenManager;
import site.easy.to.build.crm.google.model.gmail.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;

@Service
public class GmailEmailService {
    private final GoogleGmailApiService googleGmailApiService;

    @Autowired
    public GmailEmailService(GoogleGmailApiService googleGmailApiService) {
        this.googleGmailApiService = googleGmailApiService;
    }

    public String getPageTokenForPage(PageTokenManager pageTokenManager, int page, OAuthUser oAuthUser, int maxResult, String label)
            throws GeneralSecurityException, IOException {

        String pageToken = pageTokenManager.getPageToken(page);
        if (pageToken == null) {
            Integer closestPageNumber = pageTokenManager.findClosestPageNumber(page);
            if (closestPageNumber != null) {
                pageToken = pageTokenManager.getPageToken(closestPageNumber);
                EmailPage emailsPerPage = googleGmailApiService.listAndReadEmails(oAuthUser, maxResult, pageToken, label);

                // Navigate to the desired page
                int steps = page - closestPageNumber - 1;
                for (int i = 0; i < Math.abs(steps); i++) {
                    emailsPerPage = googleGmailApiService.listAndReadEmails(oAuthUser, maxResult, emailsPerPage.getNextPageToken(), label);
                }
                pageToken = emailsPerPage.getNextPageToken();
            } else {
                // Return null for the first page
                return null;
            }
        }
        return pageToken;
    }

    public EmailPage getEmailsPerPage(OAuthUser oAuthUser, int maxResult, String pageToken, String label) {
        EmailPage emailsPerPage;
        try {
            emailsPerPage = googleGmailApiService.listAndReadEmails(oAuthUser, maxResult, pageToken, label);
        } catch (IOException | GeneralSecurityException e) {
            emailsPerPage = new EmailPage();
            emailsPerPage.setEmails(new ArrayList<>());
        }
        return emailsPerPage;
    }

    public int getEmailsCountInInbox(OAuthUser oAuthUser, String query) throws IOException, GeneralSecurityException {
        return googleGmailApiService.getEmailsCount(oAuthUser,query);
    }

    public void deleteEmail(OAuthUser oAuthUser, String emailId, RedirectAttributes redirectAttributes) {
        try {
            googleGmailApiService.deleteEmail(oAuthUser, emailId);
            redirectAttributes.addFlashAttribute("success", "Email successfully deleted.");
        } catch (IOException | GeneralSecurityException e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while deleting the email: " + e.getMessage());
        }
    }

    public  GmailEmailInfo getEmailDetails(OAuthUser oAuthUser, String emailId) throws GeneralSecurityException, IOException {
        return googleGmailApiService.getEmailDetails(oAuthUser, emailId);
    }

}