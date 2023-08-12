package site.easy.to.build.crm.google.model.drive;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class GoogleDriveFile {
    private String id;

    @NotBlank(message = "File name is required")
    private String name;
    @NotBlank(message = "File type is required")
    @Pattern(regexp = "^(doc|sheet|slide)$", message = "Invalid option selected")
    private String mimeType;
    private String webViewLink;
    private String createdTime;
    private String folderId;
    private String failedErrorMessage;

    // Getters and setters for each field

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getWebViewLink() {
        return webViewLink;
    }

    public void setWebViewLink(String webViewLink) {
        this.webViewLink = webViewLink;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getFailedErrorMessage() {
        return failedErrorMessage;
    }

    public void setFailedErrorMessage(String failedErrorMessage) {
        this.failedErrorMessage = failedErrorMessage;
    }
}