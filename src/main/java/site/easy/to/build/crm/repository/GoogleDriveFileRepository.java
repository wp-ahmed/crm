package site.easy.to.build.crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import site.easy.to.build.crm.entity.GoogleDriveFile;

import java.util.List;

@Repository
public interface GoogleDriveFileRepository extends JpaRepository<GoogleDriveFile, Integer> {
    public List<GoogleDriveFile> findByLeadLeadId(int leadId);

    public List<GoogleDriveFile> findByContractContractId(int contractId);

    void deleteById(int id);
}
