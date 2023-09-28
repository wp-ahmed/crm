package site.easy.to.build.crm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.EmailTemplate;

import java.util.List;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate,Integer> {
    public EmailTemplate findByTemplateId(int id);
    public EmailTemplate findByName(String name);
    public List<EmailTemplate> findAll();
    public List<EmailTemplate> findByUserId(int userId);
    public List<EmailTemplate> findTopNByOrderByCreatedAtDesc(int limit, Pageable pageable);

}
