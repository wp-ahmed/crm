package site.easy.to.build.crm.service.settings;

import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.settings.LeadEmailSettings;
import site.easy.to.build.crm.repository.settings.LeadEmailSettingsRepository;

@Service
public class LeadEmailSettingsService {

    private final LeadEmailSettingsRepository leadEmailSettingsRepository;

    public LeadEmailSettingsService(LeadEmailSettingsRepository leadEmailSettingsRepository) {
        this.leadEmailSettingsRepository = leadEmailSettingsRepository;
    }

    public void save(LeadEmailSettings leadEmailSettings) {
        leadEmailSettingsRepository.save(leadEmailSettings);
    }

    public LeadEmailSettings findByUserId(int userId) {
        return leadEmailSettingsRepository.findByUserId(userId);
    }

    public LeadEmailSettings findByCustomerId(int customerId) {
        return leadEmailSettingsRepository.findByCustomerLoginInfoId(customerId);
    }
}
