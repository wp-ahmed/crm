package site.easy.to.build.crm.service.file;

import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.File;
import site.easy.to.build.crm.repository.FileRepository;

import java.util.List;

@Service
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;

    public FileServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Override
    public List<File> findByLeadId(int leadId) {
        return fileRepository.findByLeadLeadId(leadId);
    }

    @Override
    public List<File> getContractFiles(int contractId) {
        return fileRepository.findByContractContractId(contractId);
    }

    @Override
    public void save(File file) {
        fileRepository.save(file);
    }

    @Override
    public void delete(File file) {
        fileRepository.delete(file);
    }
}
