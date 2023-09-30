package site.easy.to.build.crm.service.drive;

import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.GoogleDriveFile;

import java.util.List;

public interface GoogleDriveFileService {
    public List<GoogleDriveFile> getAllDriveFileByLeadId(int leadId);

    public List<GoogleDriveFile> getAllDriveFileByContactId(int contractId);

    public void save(GoogleDriveFile googleDriveFile);

    public void delete(int id);
}
