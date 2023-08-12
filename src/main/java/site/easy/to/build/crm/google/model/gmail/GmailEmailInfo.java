package site.easy.to.build.crm.google.model.gmail;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class GmailEmailInfo {
    private String id;
    private String threadId;
    private String snippet;
    private String subject;
    private String sender;
    private String body;
    private String senderName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String recipient;
    private String recipientName;
    private String draftId;
    private boolean read;
    private Long internalDate;
    private List<String> labelIds;
    private List<CustomHeader> headers;
    private List<Attachment> attachments;
    private String gravatarUrl;
    private String tempFilesSessionKey;
    private String failedErrorMessage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
        setGravatarUrl(generateGravatarUrl(80));
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getDraftId() {
        return draftId;
    }

    public void setDraftId(String draftId) {
        this.draftId = draftId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public long getInternalDate() {
        return internalDate;
    }

    public void setInternalDate(long internalDate) {
        this.internalDate = internalDate;
    }

    public List<String> getLabelIds() {
        return labelIds;
    }

    public void setLabelIds(List<String> labelIds) {
        this.labelIds = labelIds;
    }

    public List<CustomHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<CustomHeader> headers) {
        this.headers = headers;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public String getGravatarUrl() {
        return gravatarUrl;
    }

    public void setGravatarUrl(String gravatarUrl) {
        this.gravatarUrl = gravatarUrl;
    }

    public String getTempFilesSessionKey() {
        return tempFilesSessionKey;
    }

    public void setTempFilesSessionKey(String tempFilesSessionKey) {
        this.tempFilesSessionKey = tempFilesSessionKey;
    }

    public String getFailedErrorMessage() {
        return failedErrorMessage;
    }

    public void setFailedErrorMessage(String failedErrorMessage) {
        this.failedErrorMessage = failedErrorMessage;
    }

    private String generateGravatarUrl(int size) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(getSender().toLowerCase().getBytes(StandardCharsets.UTF_8));
            StringBuilder md5Hex = new StringBuilder();
            for (byte b : digest) {
                md5Hex.append(String.format("%02x", b));
            }
            return "https://www.gravatar.com/avatar/" + md5Hex.toString() + "?s=" + size;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
