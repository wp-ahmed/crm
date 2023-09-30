package site.easy.to.build.crm.service.lead;

import org.springframework.stereotype.Service;
import site.easy.to.build.crm.repository.LeadActionRepository;
import site.easy.to.build.crm.entity.LeadAction;

@Service
public class LeadActionServiceImpl implements LeadActionService{

    private final LeadActionRepository leadActionRepository;

    public LeadActionServiceImpl(LeadActionRepository leadActionRepository) {
        this.leadActionRepository = leadActionRepository;
    }

    @Override
    public void save(LeadAction leadAction) {
        leadActionRepository.save(leadAction);
    }
}
