package site.easy.to.build.crm.repository.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.settings.ContractEmailSettings;

@Repository
public interface ContractEmailSettingsRepository  extends JpaRepository<ContractEmailSettings, Integer>{
    public ContractEmailSettings findByUserId(int userId);

    public ContractEmailSettings findByCustomerLoginInfoId(int customerId);
}
