package site.easy.to.build.crm.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import site.easy.to.build.crm.customValidations.user.UniqueEmail;
import site.easy.to.build.crm.customValidations.user.UniqueUsername;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    public interface ValidationGroupInclusion {}
    public interface RegistrationValidation {}
    public interface SetEmployeePasswordValidation {}
    public interface ManagerUpdateValidationGroupInclusion {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", unique = true)
    @NotBlank(message = "Username is required", groups = {RegistrationValidation.class})
    @UniqueUsername(groups = {RegistrationValidation.class})
    private String username;

    @Column(name = "email")
    @NotBlank(message = "Email is required", groups = {Default.class, ValidationGroupInclusion.class, RegistrationValidation.class})
    @Email(message = "Please enter a valid email format", groups = {Default.class, ValidationGroupInclusion.class, RegistrationValidation.class})
    @UniqueEmail(groups = {Default.class, ValidationGroupInclusion.class, RegistrationValidation.class})
    private String email;


    @Column(name = "password")
    @NotBlank(message = "Password is required", groups = {RegistrationValidation.class,SetEmployeePasswordValidation.class})
    private String password;

    @Column(name = "status")
    @NotBlank(message = "Status is required", groups = {Default.class, ValidationGroupInclusion.class,ManagerUpdateValidationGroupInclusion.class})
    @Pattern(regexp = "^(active|inactive|suspended)$", message = "Invalid status",
            groups = {Default.class, ValidationGroupInclusion.class, ManagerUpdateValidationGroupInclusion.class})
    private String status;

    @Column(name = "token")
    private String token;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_password_set")
    private boolean isPasswordSet;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("user")
    @PrimaryKeyJoinColumn
    private OAuthUser oauthUser;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @NotEmpty(message = "At least one role must be selected")
    private List<Role> roles;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("user")
    private UserProfile userProfile;



    public User() {

    }

    public User(String username, String email, String password, LocalDate hireDate, LocalDateTime createdAt, LocalDateTime updatedAt,
                OAuthUser oauthUser, List<Role> roles, String status, String token, boolean isPasswordSet, UserProfile userProfile) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.hireDate = hireDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.oauthUser = oauthUser;
        this.roles = roles;
        this.status = status;
        this.token = token;
        this.isPasswordSet = isPasswordSet;
        this.userProfile = userProfile;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public boolean isPasswordSet() {
        return isPasswordSet;
    }

    public void setPasswordSet(boolean passwordSet) {
        isPasswordSet = passwordSet;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public OAuthUser getOauthUser() {
        return oauthUser;
    }

    public void setOauthUser(OAuthUser oauthUser) {
        this.oauthUser = oauthUser;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role){
        this.roles.remove(role);
    }
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public boolean isInactiveUser() {
        return this.status.equals("inactive");
    }

}

