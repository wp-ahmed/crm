package site.easy.to.build.crm.service.contract;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Customer;
import site.easy.to.build.crm.repository.ContractRepository;

import java.util.List;

@Service
public class ContractServiceImpl implements ContractService {

    private final ContractRepository contractRepository;

    public ContractServiceImpl(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Override
    public Contract findByContractId(int id) {
        return contractRepository.findByContractId(id);
    }

    @Override
    public List<Contract> findAll() {
        return contractRepository.findAll();
    }

    @Override
    public List<Contract> getCustomerContracts(int customerId) {
        return contractRepository.findByCustomerCustomerId(customerId);
    }

    @Override
    public List<Contract> getEmployeeCreatedContracts(int userId) {
        return contractRepository.findByUserId(userId);
    }

    @Override
    public Contract save(Contract contract) {
        contractRepository.save(contract);
        return contract;
    }

    @Override
    public void delete(Contract contract) {
        contractRepository.delete(contract);
    }

    @Override
    public List<Contract> getRecentContracts(int userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return contractRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public List<Contract> getRecentCustomerContracts(int customerId, int limit) {
        Pageable pageable = PageRequest.of(0,limit);
        return contractRepository.findByCustomerCustomerIdOrderByCreatedAtDesc(customerId, pageable);
    }

    @Override
    public long countByCustomerId(int customerId) {
        return contractRepository.countByCustomerCustomerId(customerId);
    }

    @Override
    public long countByUserId(int userId) {
        return contractRepository.countByUserId(userId);
    }

    @Override
    public void deleteAllByCustomer(Customer customer) {
        contractRepository.deleteAllByCustomer(customer);
    }
}
