package site.easy.to.build.crm.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_template")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "templateId")
public class EmailTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Integer templateId;

    @Column(name = "name")
    private String name;

    @Column(name = "content")
    private String content;

    @Column(name = "json_design")
    private String jsonDesign;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties("emailTemplate")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public EmailTemplate() {
    }

    public EmailTemplate(String name, String content, String jsonDesign, User user, LocalDateTime createdAt) {
        this.name = name;
        this.content = content;
        this.jsonDesign = jsonDesign;
        this.user = user;
        this.createdAt = createdAt;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getJsonDesign() {
        return jsonDesign;
    }

    public void setJsonDesign(String jsonDesign) {
        this.jsonDesign = jsonDesign;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
