package site.easy.to.build.crm.service.contract;

import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Customer;

import java.util.List;

public interface ContractService {
    public Contract findByContractId(int id);

    public List<Contract> findAll();

    public List<Contract> getCustomerContracts(int customerId);

    public List<Contract> getEmployeeCreatedContracts(int userId);

    public Contract save(Contract contract);

    public void delete(Contract contract);

    public List<Contract> getRecentContracts(int userId, int limit);

    public List<Contract> getRecentCustomerContracts(int customerId, int limit);

    public long countByCustomerId(int customerId);

    public long countByUserId(int userId);

    public void deleteAllByCustomer(Customer customer);
}
