package site.easy.to.build.crm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import site.easy.to.build.crm.customValidations.FutureDate;
import site.easy.to.build.crm.customValidations.contract.StartDateBeforeEndDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trigger_contract")
@StartDateBeforeEndDate
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contract_id")
    private int contractId;

    @Column(name = "subject")
    @NotBlank(message = "Subject is required")
    private String subject;

    @Column(name = "status")
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(expired|canceled|archived|active)$", message = "Invalid status")
    private String status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    @NotBlank(message = "Start Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Invalid date format. Expected format: yyyy-MM-dd")
    @FutureDate
    private String startDate;

    @Column(name = "end_date", nullable = false)
    @NotBlank(message = "End Date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Invalid date format. Expected format: yyyy-MM-dd")
    @FutureDate
    private String endDate;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Amount is required")
    @Digits(integer = 10, fraction = 2, message = "Amount must be a valid number with up to 2 decimal places")
    @DecimalMin(value = "0.00", inclusive = true, message = "Amount must be greater than or equal to 0.00")
    @DecimalMax(value = "9999999.99", inclusive = true, message = "Amount must be less than or equal to 9999999.99")
    private BigDecimal amount;

    @Column(name = "google_drive")
    private Boolean googleDrive;

    @Column(name = "google_drive_folder_id")
    private String googleDriveFolderId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    private List<File> files;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL)
    private List<GoogleDriveFile> googleDriveFiles;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Contract() {
    }

    public Contract(String subject, String status, String description, String startDate, String endDate, BigDecimal amount, Boolean googleDrive,
                    String googleDriveFolderId, Lead lead, User user, Customer customer, List<File> files, List<GoogleDriveFile> googleDriveFiles, LocalDateTime createdAt) {
        this.subject = subject;
        this.status = status;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amount = amount;
        this.googleDrive = googleDrive;
        this.googleDriveFolderId = googleDriveFolderId;
        this.lead = lead;
        this.user = user;
        this.customer = customer;
        this.files = files;
        this.googleDriveFiles = googleDriveFiles;
        this.createdAt = createdAt;
    }

    public int getContractId() {
        return contractId;
    }

    public void setContractId(int contractId) {
        this.contractId = contractId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public Lead getLead() {
        return lead;
    }

    public void setLead(Lead lead) {
        this.lead = lead;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}