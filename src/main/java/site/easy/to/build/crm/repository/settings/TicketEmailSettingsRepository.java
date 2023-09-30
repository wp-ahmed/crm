package site.easy.to.build.crm.repository.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.settings.TicketEmailSettings;

@Repository
public interface TicketEmailSettingsRepository  extends JpaRepository<TicketEmailSettings, Integer>{

    public TicketEmailSettings findByUserId(int userId);

    public TicketEmailSettings findByCustomerLoginInfoId(int customerId);
}
