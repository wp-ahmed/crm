package site.easy.to.build.crm.google.service.gmail;

import com.google.api.client.http.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.model.gmail.*;
import site.easy.to.build.crm.google.util.GoogleApiHelper;
import site.easy.to.build.crm.google.util.GsonUtil;
import site.easy.to.build.crm.service.user.OAuthUserService;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class GoogleGmailApiServiceImpl implements GoogleGmailApiService {

    private static final String GMAIL_API_BASE_URL = "https://www.googleapis.com/gmail/v1/users/me";

    private final OAuthUserService oAuthUserService;

    public GoogleGmailApiServiceImpl(OAuthUserService oAuthUserService) {
        this.oAuthUserService = oAuthUserService;
    }

    @Override
    public void sendEmail(OAuthUser oAuthUser, String to, String subject, String body) throws IOException, GeneralSecurityException {
        String rawEmail = GoogleApiHelper.createRawEmail(to, subject, body);
        JsonObject email = new JsonObject();
        email.addProperty("raw", rawEmail);

        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        HttpContent httpContent = ByteArrayContent.fromString("application/json", email.toString());

        GenericUrl sendUrl = new GenericUrl(GMAIL_API_BASE_URL + "/messages/send");
        HttpRequest request = httpRequestFactory.buildPostRequest(sendUrl, httpContent);
        request.execute();
    }
    public void sendEmail(OAuthUser oAuthUser, String to, String subject, String body, List<File> attachments, List<Attachment> initAttachment) throws IOException, GeneralSecurityException, MessagingException {
        String rawEmail = GoogleApiHelper.createRawEmailWithAttachments(to, subject, body, attachments, initAttachment);
        JsonObject email = new JsonObject();
        email.addProperty("raw", rawEmail);

        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        HttpContent httpContent = ByteArrayContent.fromString("application/json", email.toString());

        GenericUrl sendUrl = new GenericUrl(GMAIL_API_BASE_URL + "/messages/send");
        HttpRequest request = httpRequestFactory.buildPostRequest(sendUrl, httpContent);
        request.execute();
    }
    @Override
    public EmailPage listAndReadEmails(OAuthUser oAuthUser, int maxResults, String pageToken, String label) throws IOException, GeneralSecurityException {
        Map<String, String> queryParameters = buildEmailListQueryParameters(maxResults, pageToken, label, null);
        return getEmailsByQueryParameters(oAuthUser, maxResults, pageToken, queryParameters);
    }

    public EmailPage listAndReadEmailsByLabelId(OAuthUser oAuthUser, int maxResults, String pageToken, String labelId) throws IOException, GeneralSecurityException {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("labelIds", labelId);
        queryParameters.put("maxResults", Integer.toString(maxResults));
        queryParameters.put("orderBy", "date");
        Optional.ofNullable(pageToken).ifPresent(token -> queryParameters.put("pageToken", token));
        return getEmailsByQueryParameters(oAuthUser, maxResults, pageToken, queryParameters);
    }

    @Override
    public int getEmailsCount(OAuthUser oAuthUser, String query) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        GenericUrl messagesUrl = new GenericUrl(GMAIL_API_BASE_URL + "/messages");
        messagesUrl.put("q", query);
        messagesUrl.put("maxResults", 500); // Set maxResults to 500

        int count = 0;
        String nextPageToken = null;

        do {
            if (nextPageToken != null) {
                messagesUrl.put("pageToken", nextPageToken);
            }

            JsonObject jsonResponse = executeRequest(httpRequestFactory, messagesUrl);
            JsonArray messages = jsonResponse.getAsJsonArray("messages");

            if (messages != null) {
                count += messages.size();
            }

            nextPageToken = jsonResponse.has("nextPageToken") ? jsonResponse.get("nextPageToken").getAsString() : null;
        } while (nextPageToken != null);

        return count;
    }
    @Override
    public void deleteEmail(OAuthUser oAuthUser, String emailId) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        // Construct the URL for deleting the email with the given emailId
        GenericUrl deleteUrl = new GenericUrl(GMAIL_API_BASE_URL + "/messages/" + emailId);

        // Create and execute a DELETE request
        HttpRequest deleteRequest = httpRequestFactory.buildDeleteRequest(deleteUrl);
        deleteRequest.execute();
    }

    @Override
    public void replyToEmail(OAuthUser oAuthUser, String emailId, String body) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        // Fetch original email details
        GmailEmailInfo originalEmailInfo = fetchEmailInfo(httpRequestFactory, emailId,accessToken);

        String to = originalEmailInfo.getSender();
        String subject = "Re: " + originalEmailInfo.getSubject();

        // Send the reply email
        sendEmail(oAuthUser, to, subject, body);
    }

    @Override
    public void forwardEmail(OAuthUser oAuthUser, String emailId, String to, String body) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        // Fetch original email details
        GmailEmailInfo originalEmailInfo = fetchEmailInfo(httpRequestFactory, emailId,accessToken);

        String subject = "Fwd: " + originalEmailInfo.getSubject();

        // Add original email content to the body
        String forwardedBody = body + "\n\n-------- Forwarded Message --------\n" + originalEmailInfo.getSnippet();

        // Send the forwarded email
        sendEmail(oAuthUser, to, subject, forwardedBody);
    }



    @Override
    public String createDraft(OAuthUser oAuthUser, String to, String subject, String body, List<File> attachments, List<Attachment> initAttachment) throws IOException, GeneralSecurityException, MessagingException {
        String rawEmail = GoogleApiHelper.createRawEmailWithAttachments(to, subject, body, attachments, initAttachment);
        JsonObject draftEmail = new JsonObject();
        draftEmail.addProperty("raw", rawEmail);

        JsonObject draft = new JsonObject();
        draft.add("message", draftEmail);

        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        HttpContent httpContent = ByteArrayContent.fromString("application/json", draft.toString());

        GenericUrl draftUrl = new GenericUrl(GMAIL_API_BASE_URL + "/drafts");
        HttpRequest request = httpRequestFactory.buildPostRequest(draftUrl, httpContent);
        HttpResponse response = request.execute();

        // Parse the response to get the draft ID
        JsonObject responseJson = JsonParser.parseString(response.parseAsString()).getAsJsonObject();
        return responseJson.get("id").getAsString();
    }

    @Override
    public void updateDraft(OAuthUser oAuthUser, String draftId, String to, String subject, String body, List<File> attachments, List<Attachment> initAttachment) throws IOException, GeneralSecurityException, MessagingException {
        String rawEmail = GoogleApiHelper.createRawEmailWithAttachments(to, subject, body, attachments, initAttachment);
        JsonObject draftEmail = new JsonObject();
        draftEmail.addProperty("raw", rawEmail);

        JsonObject draft = new JsonObject();
        draft.add("message", draftEmail);
        draft.addProperty("id", draftId);

        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        HttpContent httpContent = ByteArrayContent.fromString("application/json", draft.toString());

        GenericUrl updateDraftUrl = new GenericUrl(GMAIL_API_BASE_URL + "/drafts/" + draftId);
        HttpRequest request = httpRequestFactory.buildPutRequest(updateDraftUrl, httpContent);
        request.execute();
    }

    @Override
    public GmailEmailInfo getDraft(OAuthUser oAuthUser, String draftId) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        GenericUrl getDraftUrl = new GenericUrl(GMAIL_API_BASE_URL + "/drafts/" + draftId);
        JsonObject jsonResponse = executeRequest(httpRequestFactory, getDraftUrl);
        GmailApiMessage message = GsonUtil.fromJson(jsonResponse.getAsJsonObject("message"), GmailApiMessage.class);
        GmailEmailInfo gmailEmailInfo =  buildEmailInfo(message,accessToken);
        gmailEmailInfo.setDraftId(draftId);
        return gmailEmailInfo;
    }

    @Override
    public void removeDraft(OAuthUser oAuthUser, String draftId) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        GenericUrl removeDraftUrl = new GenericUrl(GMAIL_API_BASE_URL + "/drafts/" + draftId);
        HttpRequest request = httpRequestFactory.buildDeleteRequest(removeDraftUrl);
        request.execute();
    }

    @Override
    public GmailEmailInfo getEmailDetails(OAuthUser oAuthUser, String emailId) throws GeneralSecurityException, IOException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);
        return fetchEmailInfo(httpRequestFactory,emailId,accessToken);
    }

    @Override
    public void updateEmail(OAuthUser oAuthUser, String emailId) throws GeneralSecurityException, IOException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);
        GenericUrl modifyUrl = GoogleApiHelper.buildGenericUrl(GMAIL_API_BASE_URL + "/messages/" + emailId + "/modify", null);

        JsonObject requestJson = new JsonObject();
        JsonArray addLabelIds = new JsonArray();
        addLabelIds.add("UNREAD");
        requestJson.add("removeLabelIds", addLabelIds);

        String requestJsonString = requestJson.toString();

        HttpContent httpContent = ByteArrayContent.fromString("application/json", requestJsonString);

        HttpRequest request = httpRequestFactory.buildPostRequest(modifyUrl, httpContent);
        request.execute();
    }
    private GmailEmailInfo fetchEmailInfo(HttpRequestFactory httpRequestFactory, String emailId, String accessToken) throws IOException {
        GenericUrl emailUrl = GoogleApiHelper.buildGenericUrl(GMAIL_API_BASE_URL + "/messages/" + emailId, null);
        JsonObject jsonResponse = executeRequest(httpRequestFactory, emailUrl);

        GmailApiMessage message = GsonUtil.fromJson(jsonResponse, GmailApiMessage.class);

        return buildEmailInfo(message,accessToken);
    }
    private Map<String, String> buildEmailListQueryParameters(int maxResults, String pageToken, String label, String labelId) {
        Map<String, String> queryParameters = new HashMap<>();
        queryParameters.put("maxResults", Integer.toString(maxResults));
        queryParameters.put("orderBy", "date");
        Optional.ofNullable(pageToken).ifPresent(token -> queryParameters.put("pageToken", token));

        if (label != null) {
            switch (label) {
                case "inbox" -> queryParameters.put("q", "category:primary");
                case "sent" -> queryParameters.put("labelIds", "SENT");
                case "draft" -> queryParameters.put("labelIds", "DRAFT");
                case "unread" -> queryParameters.put("q", "is:unread");
                case "starred" -> queryParameters.put("labelIds", "STARRED");
                case "trash" -> queryParameters.put("labelIds", "TRASH");
                case "custom" -> queryParameters.put("labelIds", labelId);
            }
        }

        return queryParameters;
    }

    private EmailPage getEmailsByQueryParameters(OAuthUser oAuthUser, int maxResults, String pageToken, Map<String, String> queryParameters) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        List<GoogleGmailEmail> emails = fetchEmails(httpRequestFactory, queryParameters);
        if(emails==null) {
            return new EmailPage();
        }
        List<GmailEmailInfo> emailsInformation = emails.stream()
                .map(email -> {
                    try {
                        return (email.getThreadId() != null) ? fetchEmailInfo(httpRequestFactory, email.getId(), accessToken) : getDraft(oAuthUser,email.getId());
                    } catch (IOException | GeneralSecurityException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());

        EmailPage emailsPerPage = new EmailPage();
        emailsPerPage.setEmails(emailsInformation);
        emailsPerPage.setNextPageToken(getNextPageToken(httpRequestFactory, queryParameters));

        return emailsPerPage;
    }

    private List<GoogleGmailEmail> fetchEmails(HttpRequestFactory httpRequestFactory, Map<String, String> queryParameters) throws IOException {
        GenericUrl listUrl;
        boolean isDraft = false;
        if(queryParameters.containsKey("labelIds") && queryParameters.get("labelIds").equals("DRAFT")){
            listUrl = GoogleApiHelper.buildGenericUrl(GMAIL_API_BASE_URL + "/drafts", queryParameters);
            isDraft = true;
        }else {
            listUrl = GoogleApiHelper.buildGenericUrl(GMAIL_API_BASE_URL + "/messages", queryParameters);
        }
        JsonObject jsonResponse = executeRequest(httpRequestFactory, listUrl);

        JsonArray messagesArray = (isDraft) ? jsonResponse.getAsJsonArray("drafts") : jsonResponse.getAsJsonArray("messages");
        Type emailListType = new TypeToken<List<GoogleGmailEmail>>() {}.getType();
        return GsonUtil.fromJson(messagesArray, emailListType);
    }

    private String getNextPageToken(HttpRequestFactory httpRequestFactory, Map<String, String> queryParameters) throws IOException {
        GenericUrl listUrl = GoogleApiHelper.buildGenericUrl(GMAIL_API_BASE_URL + "/messages", queryParameters);
        JsonObject jsonResponse = executeRequest(httpRequestFactory, listUrl);
        return jsonResponse.has("nextPageToken") ? jsonResponse.get("nextPageToken").getAsString() : null;
    }
    private JsonObject executeRequest(HttpRequestFactory httpRequestFactory, GenericUrl url) throws IOException {
        HttpRequest request = httpRequestFactory.buildGetRequest(url);
        HttpResponse response = request.execute();
        String responseBody = response.parseAsString();
        return GsonUtil.fromJson(responseBody);
    }

    private GmailEmailInfo buildEmailInfo(GmailApiMessage message, String accessToken) {
        GmailApiMessage x = message;
        GmailEmailInfo emailInfo = new GmailEmailInfo();
        emailInfo.setId(message.getId());
        emailInfo.setHeaders(message.getPayload().getHeaders());
        emailInfo.setThreadId(message.getThreadId());
        emailInfo.setLabelIds(message.getLabelIds());
        emailInfo.setInternalDate(message.getInternalDate());
        emailInfo.setSnippet(message.getSnippet());
        emailInfo.setSubject(message.getHeaderValue("subject"));

        String[] toParts = message.extractEmailParts(message.getHeaderValue("to"));
        emailInfo.setRecipient(toParts[0]);
        emailInfo.setRecipientName(toParts[1]);

        String[] fromParts = message.extractEmailParts(message.getHeaderValue("from"));
        emailInfo.setSender(fromParts[0]);
        emailInfo.setSenderName(fromParts[1]);

        List<Part> parts = message.getPayload().getParts();
        List<Attachment> attachments = getAttachmentDetails(parts,message,accessToken);
        emailInfo.setAttachments(attachments);

        emailInfo.setBody(GoogleApiHelper.getEmailBody(message,accessToken,message.getId()));
        emailInfo.setRead(!message.getLabelIds().contains("UNREAD"));

        return emailInfo;
    }
    private List<Attachment> getAttachmentDetails(List<Part> parts, GmailApiMessage message, String accessToken){
        List<Attachment> attachments = new ArrayList<>();
        if(parts != null) {
            for (Part part : parts) {
                if (part.getBody().getAttachmentId() != null) {
                    String imageData;
                    try {
                        imageData = GoogleApiHelper.getAttachmentData(part.getBody().getAttachmentId(), message.getId(), accessToken);
                        String replaceString = imageData.replaceAll("_", "/");
                        String data = replaceString.replaceAll("-", "+");
                        String fileName="";
                        String value = part.getHeaders().get(0).getValue();
                        int size = part.getBody().getSize();
                        int nameIndex = value.indexOf("name=");
                        if(nameIndex != -1) {
                            fileName = value.substring(nameIndex + 5).replace("\"", "").trim();
                        }
                        attachments.add(new Attachment(fileName,data,part.getMimeType(),size));
                    } catch (GeneralSecurityException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return attachments;
    }
}
