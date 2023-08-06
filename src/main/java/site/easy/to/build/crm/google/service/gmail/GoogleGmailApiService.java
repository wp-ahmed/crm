package site.easy.to.build.crm.google.service.gmail;

import com.google.api.client.http.*;
import jakarta.mail.MessagingException;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.model.gmail.EmailPage;
import site.easy.to.build.crm.google.model.gmail.*;
import site.easy.to.build.crm.google.util.GoogleApiHelper;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface GoogleGmailApiService {
    void sendEmail(OAuthUser oAuthUser, String to, String subject, String body) throws IOException, GeneralSecurityException;

    public void sendEmail(OAuthUser oAuthUser, String to, String subject, String body, List<File> attachments, List<Attachment> initAttachment) throws IOException, GeneralSecurityException, MessagingException;

    public EmailPage listAndReadEmails(OAuthUser oAuthUser, int maxResults, String pageToken, String label) throws IOException, GeneralSecurityException;

    public int getEmailsCount(OAuthUser oAuthUser, String query) throws IOException, GeneralSecurityException;

    void deleteEmail(OAuthUser oAuthUser, String emailId) throws IOException, GeneralSecurityException;

    public void replyToEmail(OAuthUser oAuthUser, String emailId, String body) throws IOException, GeneralSecurityException;

    public void forwardEmail(OAuthUser oAuthUser, String emailId, String to, String body) throws IOException, GeneralSecurityException;

    public GmailEmailInfo getEmailDetails(OAuthUser oAuthUser, String emailId) throws GeneralSecurityException, IOException;

    public String createDraft(OAuthUser oAuthUser, String to, String subject, String body, List<File> attachments, List<Attachment> initAttachment) throws IOException, GeneralSecurityException, MessagingException;
    public void updateDraft(OAuthUser oAuthUser, String draftId, String to, String subject, String body, List<File> attachments, List<Attachment> initAttachment) throws IOException, GeneralSecurityException, MessagingException;
    public GmailEmailInfo getDraft(OAuthUser oAuthUser, String draftId) throws IOException, GeneralSecurityException;
    public void removeDraft(OAuthUser oAuthUser, String draftId) throws IOException, GeneralSecurityException;

    public void updateEmail(OAuthUser oAuthUser, String emailId) throws GeneralSecurityException, IOException;
}
