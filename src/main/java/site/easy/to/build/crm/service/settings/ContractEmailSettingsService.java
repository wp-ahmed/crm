package site.easy.to.build.crm.service.settings;

import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.settings.ContractEmailSettings;
import site.easy.to.build.crm.repository.settings.ContractEmailSettingsRepository;

@Service
public class ContractEmailSettingsService {

    private final ContractEmailSettingsRepository contractEmailSettingsRepository;

    public ContractEmailSettingsService(ContractEmailSettingsRepository contractEmailSettingsRepository) {
        this.contractEmailSettingsRepository = contractEmailSettingsRepository;
    }

    public void save(ContractEmailSettings contractEmailSettings) {
        contractEmailSettingsRepository.save(contractEmailSettings);
    }

    public ContractEmailSettings findByUserId(int userId) {
        return contractEmailSettingsRepository.findByUserId(userId);
    }

    public ContractEmailSettings findByCustomerId(int customerId) {
        return contractEmailSettingsRepository.findByCustomerLoginInfoId(customerId);
    }
}
