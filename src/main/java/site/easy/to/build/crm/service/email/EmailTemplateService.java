package site.easy.to.build.crm.service.email;

import org.springframework.security.core.Authentication;
import site.easy.to.build.crm.entity.EmailTemplate;

import java.util.List;

public interface EmailTemplateService {
    public EmailTemplate findByTemplateId(int id);

    public EmailTemplate findByName(String name);

    public List<EmailTemplate> getAllTemplates();

    public void save(EmailTemplate emailTemplate, Authentication authentication);

    public List<EmailTemplate> findByUserId(int userId);

    public void delete(int id);
}
