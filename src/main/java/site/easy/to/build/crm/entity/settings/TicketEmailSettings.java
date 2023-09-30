package site.easy.to.build.crm.entity.settings;

import jakarta.persistence.*;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.entity.EmailTemplate;
import site.easy.to.build.crm.entity.User;

@Entity
@Table(name = "ticket_settings")
public class TicketEmailSettings extends EmailSettings{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "priority")
    private Boolean priority;

    @Column(name = "subject")
    private Boolean subject;

    @Column(name = "description")
    private Boolean description;

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
    @JoinColumn(name = "subject_email_template")
    private EmailTemplate subjectEmailTemplate;

    @OneToOne
    @JoinColumn(name = "priority_email_template")
    private EmailTemplate priorityEmailTemplate;

    @OneToOne
    @JoinColumn(name = "description_email_template")
    private EmailTemplate descriptionEmailTemplate;

    public TicketEmailSettings() {
    }

    public TicketEmailSettings(Boolean priority, Boolean subject, Boolean description, Boolean status, User user,
                               EmailTemplate statusEmailTemplate, EmailTemplate subjectEmailTemplate, EmailTemplate priorityEmailTemplate,
                               EmailTemplate descriptionEmailTemplate, CustomerLoginInfo customerLoginInfo) {
        this.priority = priority;
        this.subject = subject;
        this.description = description;
        this.status = status;
        this.user = user;
        this.statusEmailTemplate = statusEmailTemplate;
        this.subjectEmailTemplate = subjectEmailTemplate;
        this.priorityEmailTemplate = priorityEmailTemplate;
        this.descriptionEmailTemplate = descriptionEmailTemplate;
        this.customerLoginInfo = customerLoginInfo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getPriority() {
        return priority;
    }

    public void setPriority(Boolean priority) {
        this.priority = priority;
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

    public EmailTemplate getSubjectEmailTemplate() {
        return subjectEmailTemplate;
    }

    public void setSubjectEmailTemplate(EmailTemplate subjectEmailTemplate) {
        this.subjectEmailTemplate = subjectEmailTemplate;
    }

    public EmailTemplate getPriorityEmailTemplate() {
        return priorityEmailTemplate;
    }

    public void setPriorityEmailTemplate(EmailTemplate priorityEmailTemplate) {
        this.priorityEmailTemplate = priorityEmailTemplate;
    }

    public EmailTemplate getDescriptionEmailTemplate() {
        return descriptionEmailTemplate;
    }

    public void setDescriptionEmailTemplate(EmailTemplate descriptionEmailTemplate) {
        this.descriptionEmailTemplate = descriptionEmailTemplate;
    }
}
