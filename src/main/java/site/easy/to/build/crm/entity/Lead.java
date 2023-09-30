package site.easy.to.build.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trigger_lead")
public class Lead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_id")
    private int leadId;

    @Column(name = "name")
    @NotBlank(message = "Name is required")
    private String name;

    @Column(name = "status")
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(meeting-to-schedule|scheduled|archived|success|assign-to-sales)$", message = "Invalid status")
    private String status;

    @Column(name = "phone")
    private String phone;

    @Column(name = "meeting_id")
    private String meetingId;

    @Column(name = "google_drive")
    private Boolean googleDrive;

    @Column(name = "google_drive_folder_id")
    private String googleDriveFolderId;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    private List<LeadAction> leadActions;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    private List<File> files;

    @OneToMany(mappedBy = "lead", cascade = CascadeType.ALL)
    private List<GoogleDriveFile> googleDriveFiles;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User manager;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private User employee;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Lead() {
    }

    public Lead(String name, String status, String phone, String meetingId, Boolean googleDrive, String googleDriveFolderId,
                List<LeadAction> leadActions, List<File> files, List<GoogleDriveFile> googleDriveFiles, User manager, User employee,
                Customer customer, LocalDateTime createdAt) {
        this.name = name;
        this.status = status;
        this.phone = phone;
        this.meetingId = meetingId;
        this.googleDrive = googleDrive;
        this.googleDriveFolderId = googleDriveFolderId;
        this.leadActions = leadActions;
        this.files = files;
        this.googleDriveFiles = googleDriveFiles;
        this.manager = manager;
        this.employee = employee;
        this.customer = customer;
        this.createdAt = createdAt;
    }

    public int getLeadId() {
        return leadId;
    }

    public void setLeadId(int leadId) {
        this.leadId = leadId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public Boolean getGoogleDrive() {
        return googleDrive;
    }

    public void setGoogleDrive(Boolean googleDrive) {
        this.googleDrive = googleDrive;
    }

    public String getGoogleDriveFolderId() {
        return googleDriveFolderId;
    }

    public void setGoogleDriveFolderId(String googleDriveFolderId) {
        this.googleDriveFolderId = googleDriveFolderId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public List<LeadAction> getLeadActions() {
        return leadActions;
    }

    public void addLeadAction(LeadAction leadAction) {
        this.leadActions.add(leadAction);
    }

    public void removeLeadAction(LeadAction leadAction) {
        this.leadActions.remove(leadAction);
    }

    public List<File> getFiles() {
        return files;
    }

    public void addFile(File file) {
        this.files.add(file);
    }

    public void removeFile(File file) {
        this.files.remove(file);
    }


    public void addGoogleDriveFile(GoogleDriveFile googleDriveFile) {
        this.googleDriveFiles.add(googleDriveFile);
    }

    public void removeGoogleDriveFile(GoogleDriveFile googleDriveFile) {
        this.googleDriveFiles.remove(googleDriveFile);
    }

    public List<GoogleDriveFile> getGoogleDriveFiles() {
        return googleDriveFiles;
    }

    public void setGoogleDriveFiles(List<GoogleDriveFile> googleDriveFiles) {
        this.googleDriveFiles = googleDriveFiles;
    }

    public User getManager() {
        return manager;
    }

    public void setManager(User manager) {
        this.manager = manager;
    }

    public User getEmployee() {
        return employee;
    }

    public void setEmployee(User employee) {
        this.employee = employee;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public void setLeadActions(List<LeadAction> leadActions) {
        this.leadActions = leadActions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


