package site.easy.to.build.crm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "file")
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private int fileId;

    @Column(name = "file_name")
    private String fileName;

    @Lob
    @Column(name = "file_data")
    private byte[] fileData;

    @Column(name = "file_type")
    private String fileType;

    @ManyToOne
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    public File() {
    }

    public File(String fileName, byte[] fileData, String fileType, Lead lead) {
        this.fileName = fileName;
        this.fileData = fileData;
        this.fileType = fileType;
        this.lead = lead;
    }

    public File(String fileName, byte[] fileData, String fileType, Contract contract) {
        this.fileName = fileName;
        this.fileData = fileData;
        this.fileType = fileType;
        this.contract = contract;
    }

    public File(String fileName, byte[] fileData, String fileType) {
        this.fileName = fileName;
        this.fileData = fileData;
        this.fileType = fileType;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Lead getLead() {
        return lead;
    }

    public void setLead(Lead lead) {
        this.lead = lead;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }
}
