package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.File;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Integer> {
    public List<File> findByLeadLeadId(int leadId);

    public List<File> findByContractContractId(int contractId);
}
