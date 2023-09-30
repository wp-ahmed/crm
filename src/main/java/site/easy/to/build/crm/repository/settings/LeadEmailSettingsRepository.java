package site.easy.to.build.crm.repository.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.settings.LeadEmailSettings;

@Repository
public interface LeadEmailSettingsRepository extends JpaRepository<LeadEmailSettings,Integer> {
    public LeadEmailSettings findByUserId(int userId);

    public LeadEmailSettings findByCustomerLoginInfoId(int customerId);
}
