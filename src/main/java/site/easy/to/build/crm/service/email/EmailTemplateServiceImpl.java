package site.easy.to.build.crm.service.email;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.repository.EmailTemplateRepository;
import site.easy.to.build.crm.entity.EmailTemplate;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.user.UserService;
import site.easy.to.build.crm.util.AuthenticationUtils;

import java.util.List;

@Service
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final AuthenticationUtils authenticationUtils;
    private final UserService userService;

    public EmailTemplateServiceImpl(EmailTemplateRepository emailTemplateRepository, AuthenticationUtils authenticationUtils, UserService userService) {
        this.emailTemplateRepository = emailTemplateRepository;
        this.authenticationUtils = authenticationUtils;
        this.userService = userService;
    }


    @Override
    public EmailTemplate findByTemplateId(int id) {
        return emailTemplateRepository.findByTemplateId(id);
    }

    @Override
    public EmailTemplate findByName(String name) {
        return emailTemplateRepository.findByName(name);
    }

    @Override
    public List<EmailTemplate> getAllTemplates() {
        return emailTemplateRepository.findAll();
    }

    @Override
    public void save(EmailTemplate emailTemplate, Authentication authentication) {
        int userId = authenticationUtils.getLoggedInUserId(authentication);
        if (userId == -1) {
            throw new UsernameNotFoundException("User not found");
        }
        User user = userService.findById(userId);
        emailTemplate.setUser(user);
        emailTemplateRepository.save(emailTemplate);
    }

    @Override
    public List<EmailTemplate> findByUserId(int userId) {
        return emailTemplateRepository.findByUserId(userId);
    }

    @Override
    public void delete(int id) {
        EmailTemplate emailTemplate = emailTemplateRepository.findByTemplateId(id);
        emailTemplateRepository.delete(emailTemplate);
    }
}
