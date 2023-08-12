package site.easy.to.build.crm.google.model.drive;

import jakarta.validation.constraints.NotBlank;

public class GoogleDriveFolder {
    private String id;
    @NotBlank(message = "Folder name is required")
    private String name;

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
}
