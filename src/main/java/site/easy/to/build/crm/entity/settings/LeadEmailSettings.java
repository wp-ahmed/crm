package site.easy.to.build.crm.entity.settings;

import jakarta.persistence.*;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.entity.CustomerLoginInfo;
import site.easy.to.build.crm.entity.EmailTemplate;
import site.easy.to.build.crm.entity.User;

@Entity
@Table(name = "lead_settings")
public class LeadEmailSettings extends EmailSettings{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "meeting")
    private Boolean meeting;

    @Column(name = "phone")
    private Boolean phone;

    @Column(name = "name")
    private Boolean name;
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
    @JoinColumn(name = "phone_email_template")
    private EmailTemplate phoneEmailTemplate;

    @OneToOne
    @JoinColumn(name = "meeting_email_template")
    private EmailTemplate meetingEmailTemplate;

    @OneToOne
    @JoinColumn(name = "name_email_template")
    private EmailTemplate nameEmailTemplate;

    public LeadEmailSettings() {
    }

    public LeadEmailSettings(Boolean status, Boolean meeting, Boolean phone, Boolean name, User user,
                             EmailTemplate statusEmailTemplate, EmailTemplate phoneEmailTemplate, EmailTemplate meetingEmailTemplate,
                             EmailTemplate nameEmailTemplate, CustomerLoginInfo customerLoginInfo) {
        this.status = status;
        this.meeting = meeting;
        this.phone = phone;
        this.name = name;
        this.user = user;
        this.statusEmailTemplate = statusEmailTemplate;
        this.phoneEmailTemplate = phoneEmailTemplate;
        this.meetingEmailTemplate = meetingEmailTemplate;
        this.nameEmailTemplate = nameEmailTemplate;
        this.customerLoginInfo = customerLoginInfo;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Boolean getMeeting() {
        return meeting;
    }

    public void setMeeting(Boolean meeting) {
        this.meeting = meeting;
    }

    public Boolean getPhone() {
        return phone;
    }

    public void setPhone(Boolean phone) {
        this.phone = phone;
    }

    public Boolean getName() {
        return name;
    }

    public void setName(Boolean name) {
        this.name = name;
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

    public EmailTemplate getPhoneEmailTemplate() {
        return phoneEmailTemplate;
    }

    public void setPhoneEmailTemplate(EmailTemplate phoneEmailTemplate) {
        this.phoneEmailTemplate = phoneEmailTemplate;
    }

    public EmailTemplate getMeetingEmailTemplate() {
        return meetingEmailTemplate;
    }

    public void setMeetingEmailTemplate(EmailTemplate meetingEmailTemplate) {
        this.meetingEmailTemplate = meetingEmailTemplate;
    }

    public EmailTemplate getNameEmailTemplate() {
        return nameEmailTemplate;
    }

    public void setNameEmailTemplate(EmailTemplate nameEmailTemplate) {
        this.nameEmailTemplate = nameEmailTemplate;
    }
}
