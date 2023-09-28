package site.easy.to.build.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "customer_login_info")
public class CustomerLoginInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "token")
    private String token;

    @Column(name = "password_set")
    private Boolean passwordSet;

    @OneToOne(mappedBy = "customerLoginInfo", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("customerLoginInfo")
    @PrimaryKeyJoinColumn
    private Customer customer;


    public CustomerLoginInfo() {
    }

    public CustomerLoginInfo(String username, String password, String token, Boolean passwordSet, Customer customer) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.passwordSet = passwordSet;
        this.customer = customer;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isPasswordSet() {
        return passwordSet;
    }

    public void setPasswordSet(Boolean passwordSet) {
        this.passwordSet = passwordSet;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getEmail() {
        return username;
    }

    public void setEmail(String email) {
        this.username = email;
    }
}
