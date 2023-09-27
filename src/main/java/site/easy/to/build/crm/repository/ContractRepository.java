package site.easy.to.build.crm.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.Contract;
import site.easy.to.build.crm.entity.Customer;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer> {
    public Contract findByContractId(int contractId);

    public List<Contract> findByUserId(int userId);

    public List<Contract> findByCustomerCustomerId(int customerId);

    public List<Contract> findByUserIdOrderByCreatedAtDesc(int userId, Pageable pageable);

    public List<Contract> findByCustomerCustomerIdOrderByCreatedAtDesc(int customerId, Pageable pageable);

    long countByUserId(int userId);

    long countByCustomerCustomerId(int customerId);

    void deleteAllByCustomer(Customer customer);
}
