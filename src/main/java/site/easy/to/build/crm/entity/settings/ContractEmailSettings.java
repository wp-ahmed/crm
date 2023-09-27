package site.easy.to.build.crm.entity.settings;

import jakarta.persistence.*;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.entity.EmailTemplate;
import site.easy.to.build.crm.entity.User;

@Entity
@Table(name = "contract_settings")
public class ContractEmailSettings extends EmailSettings{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "amount")
    private Boolean amount;

    @Column(name = "subject")
    private Boolean subject;

    @Column(name = "description")
    private Boolean description;

    @Column(name = "end_date")
    private Boolean endDate;

    @Column(name = "start_date")
    private Boolean startDate;

    @Column(name = "status")
    private Boolean status;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne
    @JoinColumn(name = "customer_id")
    private CustomerLoginInfo customerLoginInfo;

    @OneToOne
    @JoinColumn(name = "status_email_template")
    private EmailTemplate statusEmailTemplate;

    @OneToOne
    @JoinColumn(name = "amount_email_template")
    private EmailTemplate amountEmailTemplate;

    @OneToOne
    @JoinColumn(name = "subject_email_template")
    private EmailTemplate subjectEmailTemplate;

    @OneToOne
    @JoinColumn(name = "description_email_template")
    private EmailTemplate descriptionEmailTemplate;

    @OneToOne
    @JoinColumn(name = "start_email_template")
    private EmailTemplate startDateEmailTemplate;

    @OneToOne
    @JoinColumn(name = "end_email_template")
    private EmailTemplate endDateEmailTemplate;

    public ContractEmailSettings() {
    }

    public ContractEmailSettings(Boolean amount, Boolean subject, Boolean description, Boolean endDate, Boolean startDate, Boolean status, User user,
                                 EmailTemplate statusEmailTemplate, EmailTemplate amountEmailTemplate, EmailTemplate subjectEmailTemplate,
                                 EmailTemplate descriptionEmailTemplate, EmailTemplate startEmailTemplate, EmailTemplate endEmailTemplate,
                                 CustomerLoginInfo customerLoginInfo) {
        this.amount = amount;
        this.subject = subject;
        this.description = description;
        this.endDate = endDate;
        this.startDate = startDate;
        this.status = status;
        this.user = user;
        this.statusEmailTemplate = statusEmailTemplate;
        this.amountEmailTemplate = amountEmailTemplate;
        this.subjectEmailTemplate = subjectEmailTemplate;
        this.descriptionEmailTemplate = descriptionEmailTemplate;
        this.startDateEmailTemplate = startEmailTemplate;
        this.endDateEmailTemplate = endEmailTemplate;
        this.customerLoginInfo = customerLoginInfo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getAmount() {
        return amount;
    }

    public void setAmount(Boolean amount) {
        this.amount = amount;
    }

    public Boolean getSubject() {
        return subject;
    }

    public void setSubject(Boolean subject) {
        this.subject = subject;
    }

    public Boolean getDescription() {
        return description;
    }

    public void setDescription(Boolean description) {
        this.description = description;
    }

    public Boolean getEndDate() {
        return endDate;
    }

    public void setEndDate(Boolean endDate) {
        this.endDate = endDate;
    }

    public Boolean getStartDate() {
        return startDate;
    }

    public void setStartDate(Boolean startDate) {
        this.startDate = startDate;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CustomerLoginInfo getCustomerLoginInfo() {
        return customerLoginInfo;
    }

    public void setCustomerLoginInfo(CustomerLoginInfo customerLoginInfo) {
        this.customerLoginInfo = customerLoginInfo;
    }

    public EmailTemplate getStatusEmailTemplate() {
        return statusEmailTemplate;
    }

    public void setStatusEmailTemplate(EmailTemplate statusEmailTemplate) {
        this.statusEmailTemplate = statusEmailTemplate;
    }

    public EmailTemplate getAmountEmailTemplate() {
        return amountEmailTemplate;
    }

    public void setAmountEmailTemplate(EmailTemplate amountEmailTemplate) {
        this.amountEmailTemplate = amountEmailTemplate;
    }

    public EmailTemplate getSubjectEmailTemplate() {
        return subjectEmailTemplate;
    }

    public void setSubjectEmailTemplate(EmailTemplate subjectEmailTemplate) {
        this.subjectEmailTemplate = subjectEmailTemplate;
    }

    public EmailTemplate getDescriptionEmailTemplate() {
        return descriptionEmailTemplate;
    }

    public void setDescriptionEmailTemplate(EmailTemplate descriptionEmailTemplate) {
        this.descriptionEmailTemplate = descriptionEmailTemplate;
    }

    public EmailTemplate getStartDateEmailTemplate() {
        return startDateEmailTemplate;
    }

    public void setStartDateEmailTemplate(EmailTemplate startDateEmailTemplate) {
        this.startDateEmailTemplate = startDateEmailTemplate;
    }

    public EmailTemplate getEndDateEmailTemplate() {
        return endDateEmailTemplate;
    }

    public void setEndDateEmailTemplate(EmailTemplate endDateEmailTemplate) {
        this.endDateEmailTemplate = endDateEmailTemplate;
    }
}
