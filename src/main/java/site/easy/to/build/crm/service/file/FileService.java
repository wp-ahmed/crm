package site.easy.to.build.crm.service.file;

import site.easy.to.build.crm.entity.File;

import java.util.List;

public interface FileService {
    public List<File> findByLeadId(int leadId);

    public List<File> getContractFiles(int contractId);

    public void save(File file);

    public void delete(File file);
}
