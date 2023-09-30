package site.easy.to.build.crm.service.settings;

import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.settings.TicketEmailSettings;
import site.easy.to.build.crm.repository.settings.TicketEmailSettingsRepository;

@Service
public class TicketEmailSettingsService {

    private final TicketEmailSettingsRepository ticketEmailSettingsRepository;

    public TicketEmailSettingsService(TicketEmailSettingsRepository ticketEmailSettingsRepository) {
        this.ticketEmailSettingsRepository = ticketEmailSettingsRepository;
    }

    public void save(TicketEmailSettings ticketEmailSettings) {
        ticketEmailSettingsRepository.save(ticketEmailSettings);
    }

    public TicketEmailSettings findByUserId(int userId) {
        return ticketEmailSettingsRepository.findByUserId(userId);
    }

    public TicketEmailSettings findByCustomerId(int customerId) {
        return ticketEmailSettingsRepository.findByCustomerLoginInfoId(customerId);
    }
}
