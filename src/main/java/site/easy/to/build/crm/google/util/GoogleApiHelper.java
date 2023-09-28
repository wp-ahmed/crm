package site.easy.to.build.crm.google.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import com.google.gson.JsonObject;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import site.easy.to.build.crm.google.model.gmail.Attachment;
import site.easy.to.build.crm.google.model.gmail.CustomHeader;
import site.easy.to.build.crm.google.model.gmail.GmailApiMessage;
import site.easy.to.build.crm.google.model.gmail.Part;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;

public class GoogleApiHelper {
    private static final String GMAIL_API_BASE_URL = "https://www.googleapis.com/gmail/v1/users/me";

    public static HttpRequestFactory createRequestFactory(String accessToken) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        return httpTransport.createRequestFactory(request -> {
            request.setParser(new JsonObjectParser(jsonFactory));
            request.getHeaders().setAuthorization("Bearer " + accessToken);
        });
    }

    public static GenericUrl buildGenericUrl(String baseURL, Map<String, String> queryParams) {
        GenericUrl genericUrl = new GenericUrl(baseURL);

        if (queryParams != null) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                genericUrl.set(entry.getKey(), entry.getValue());
            }
        }

        return genericUrl;
    }

    public static String createRawEmail(String to, String subject, String body) {
        String email = "To: " + to + "\r\n" +
                "Subject: " + subject + "\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n\r\n" +
                body;

        return Base64.getUrlEncoder().encodeToString(email.getBytes(StandardCharsets.UTF_8));
    }

    public static String createRawEmailWithAttachments(String to, String subject, String body, List<File> attachments, List<Attachment> initAttachment) throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress("me"));
        if (to != null && !to.trim().isEmpty()) {
            email.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        }
        email.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(body, "text/plain");
        multipart.addBodyPart(textPart);

        for (File attachment : attachments) {
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(attachment);
            multipart.addBodyPart(attachmentPart);
        }

        // Attach attachments
        if (initAttachment != null) {
            for (Attachment attachment : initAttachment) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                // Convert the data from Base64 to a byte array
                byte[] data = Base64.getDecoder().decode(attachment.getData());
                // Create a ByteArrayDataSource with the data, and the MIME type from the attachment
                DataSource source = new ByteArrayDataSource(data, attachment.getMimeType());
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(attachment.getName());
                multipart.addBodyPart(attachmentPart);
            }
        }

        email.setContent(multipart);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        email.writeTo(buffer);
        byte[] bytes = buffer.toByteArray();
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    public static String getEmailBody(GmailApiMessage message, String accessToken, String emailId) {
        String body = "";
        String mimeType = message.getHeaderValue("Content-Type");

        Base64.Decoder decoder = Base64.getUrlDecoder(); // Use the URL-safe Base64 decoder
        if (mimeType != null && mimeType.startsWith("text/html")) {
            byte[] data = decoder.decode(message.getPayload().getBody().getData());
            return new String(data, StandardCharsets.UTF_8);
        }
        if (mimeType != null && (mimeType.startsWith("multipart/mixed") || mimeType.startsWith("multipart/related") || mimeType.startsWith("multipart/alternative"))) {
            List<Part> parts = message.getPayload().getParts();
            Map<String, String> results = processParts(parts, decoder, accessToken, emailId);
            String plainTextBody = results.getOrDefault("plainTextBody", "");
            String htmlBody = results.getOrDefault("htmlBody", "");

            body = !htmlBody.isEmpty() ? htmlBody : plainTextBody;
        }

        return body;
    }

    private static Map<String, String> processParts(List<Part> parts, Base64.Decoder decoder, String accessToken, String emailId) {
        String plainTextBody = "";
        String htmlBody = "";
        Map<String, String> inlineImages = new HashMap<>();
        if (parts != null) {
            for (Part part : parts) {
                String partMimeType = part.getMimeType();
                if (partMimeType.equals("text/plain")) {
                    byte[] data = decoder.decode(part.getBody().getData());
                    plainTextBody = new String(data, StandardCharsets.UTF_8);
                } else if (partMimeType.equals("text/html")) {
                    byte[] data = decoder.decode(part.getBody().getData());
                    htmlBody = new String(data, StandardCharsets.UTF_8);
                } else if (partMimeType.startsWith("multipart/related") || partMimeType.startsWith("multipart/alternative")) {
                    List<Part> nestedParts = part.getParts();
                    if (nestedParts != null) {
                        Map<String, String> nestedResults = processParts(nestedParts, decoder, accessToken, emailId);
                        plainTextBody = nestedResults.getOrDefault("plainTextBody", plainTextBody);
                        htmlBody = nestedResults.getOrDefault("htmlBody", htmlBody);
                    }
                } else if (partMimeType.startsWith("image/")) {
                    String contentId = getContentId(part);
                    String contentDisposition = getContentDisposition(part);
                    if (contentId != null && !"attachment".equalsIgnoreCase(contentDisposition)) {
                        String base64Image = part.getBody().getAttachmentId();
                        if (base64Image != null) {
                            inlineImages.put(contentId, base64Image);
                        }
                    }
                }
            }

            // Replace the "cid:" references with the corresponding Base64 encoded image data
            for (Map.Entry<String, String> entry : inlineImages.entrySet()) {
                String contentId = entry.getKey();
                String attachmentId = entry.getValue();
                String imageData;
                try {
                    imageData = getAttachmentData(attachmentId, emailId, accessToken);
                } catch (IOException | GeneralSecurityException e) {
                    e.printStackTrace();
                    continue;
                }
                String replaceString = imageData.replaceAll("_", "/");
                String finl = replaceString.replaceAll("-", "+");
                String imageSrc = "data:image/*;base64," + finl;
                htmlBody = htmlBody.replace("cid:" + contentId, imageSrc);
            }
        }

        Map<String, String> results = new HashMap<>();
        results.put("plainTextBody", plainTextBody);
        results.put("htmlBody", htmlBody);
        return results;
    }

    private static String getContentDisposition(Part part) {
        if (part.getHeaders() != null) {
            for (CustomHeader header : part.getHeaders()) {
                if ("Content-Disposition".equalsIgnoreCase(header.getName())) {
                    return header.getValue();
                }
            }
        }
        return null;
    }

    private static String getContentId(Part part) {
        if (part.getHeaders() != null) {
            for (CustomHeader header : part.getHeaders()) {
                if ("Content-ID".equalsIgnoreCase(header.getName())) {
                    return header.getValue().replaceAll("[<>]", "");
                }
            }
        }
        return null;
    }

    public static String getAttachmentData(String attachmentId, String emailId, String accessToken) throws GeneralSecurityException, IOException {
        HttpRequestFactory httpRequestFactory = createRequestFactory(accessToken);
        GenericUrl attachmentUrl = new GenericUrl(GMAIL_API_BASE_URL + "/messages/" + emailId + "/attachments/" + attachmentId);
        HttpRequest request = httpRequestFactory.buildGetRequest(attachmentUrl);
        HttpResponse response = request.execute();
        JsonObject jsonResponse;
        String responseBody = response.parseAsString();
        jsonResponse = GsonUtil.fromJson(responseBody);
        return jsonResponse.get("data").getAsString();
    }
}
