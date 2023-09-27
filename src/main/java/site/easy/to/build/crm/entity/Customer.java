package site.easy.to.build.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default;
import site.easy.to.build.crm.customValidations.customer.UniqueEmail;

import java.time.LocalDateTime;

@Entity
@Table(name = "customer")
public class Customer {

    public interface CustomerUpdateValidationGroupInclusion {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;

    @Column(name = "name")
    @NotBlank(message = "Name is required", groups = {Default.class, CustomerUpdateValidationGroupInclusion.class})
    private String name;

    @Column(name = "email")
    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email format")
    @UniqueEmail
    private String email;

    @Column(name = "position")
    private String position;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    @NotBlank(message = "Country is required", groups = {Default.class, CustomerUpdateValidationGroupInclusion.class})
    private String country;

    @Column(name = "description")
    private String description;

    @Column(name = "twitter")
    private String twitter;

    @Column(name = "facebook")
    private String facebook;

    @Column(name = "youtube")
    private String youtube;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable=false)
    @JsonIgnoreProperties("customer")
    private User user;

    @OneToOne
    @JoinColumn(name = "profile_id")
    @JsonIgnore
    private CustomerLoginInfo customerLoginInfo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Customer() {
    }

    public Customer(String name, String email, String position, String phone, String address, String city, String state, String country,
                    String description, String twitter, String facebook, String youtube, User user, CustomerLoginInfo customerLoginInfo,
                    LocalDateTime createdAt) {
        this.name = name;
        this.email = email;
        this.position = position;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.country = country;
        this.description = description;
        this.twitter = twitter;
        this.facebook = facebook;
        this.youtube = youtube;
        this.user = user;
        this.customerLoginInfo = customerLoginInfo;
        this.createdAt = createdAt;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getFacebook() {
        return facebook;
    }

    public void setFacebook(String facebook) {
        this.facebook = facebook;
    }

    public String getYoutube() {
        return youtube;
    }

    public void setYoutube(String youtube) {
        this.youtube = youtube;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

//    public List<Ticket> getTickets() {
//        return tickets;
//    }
//
//    public void addTicket(Ticket ticket) {
//        this.tickets.add(ticket);
//    }
//    public void deleteTicket(Ticket ticket) {
//        this.tickets.remove(ticket);
//    }
//    public void setTickets(List<Ticket> tickets) {
//        this.tickets = tickets;
//    }
}
