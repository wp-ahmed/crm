package site.easy.to.build.crm.service.drive;

import jakarta.validation.constraints.Null;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.GoogleDriveFile;
import site.easy.to.build.crm.repository.GoogleDriveFileRepository;

import java.util.List;

@Service
public class GoogleDriveFileServiceImpl implements GoogleDriveFileService {

    private final GoogleDriveFileRepository googleDriveFileRepository;

    public GoogleDriveFileServiceImpl(GoogleDriveFileRepository googleDriveFileRepository) {
        this.googleDriveFileRepository = googleDriveFileRepository;
    }

    @Override
    public List<GoogleDriveFile> getAllDriveFileByLeadId(int leadId) {
        return googleDriveFileRepository.findByLeadLeadId(leadId);
    }

    @Override
    public List<GoogleDriveFile> getAllDriveFileByContactId(int contractId) {
        return googleDriveFileRepository.findByContractContractId(contractId);
    }

    @Override
    public void save(GoogleDriveFile googleDriveFile) {
        googleDriveFileRepository.save(googleDriveFile);
    }

    @Override
    public void delete(int id) {
        if (googleDriveFileRepository.findById(id).isPresent()) {
            googleDriveFileRepository.deleteById(id);
        }
    }
}
