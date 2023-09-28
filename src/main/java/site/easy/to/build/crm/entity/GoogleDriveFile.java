package site.easy.to.build.crm.entity;

import jakarta.mail.Folder;
import jakarta.persistence.*;

@Entity
@Table(name = "google_drive_file")
public class GoogleDriveFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "drive_file_id")
    private String driveFileId;

    @Column(name = "drive_folder_id")
    private String driveFolderId;

    @ManyToOne
    @JoinColumn(name = "lead_id")
    private Lead lead;

    @ManyToOne
    @JoinColumn(name = "contract_id")
    private Contract contract;

    public GoogleDriveFile() {
    }

    public GoogleDriveFile(String driveFileId, String driveFolderId, Lead lead) {
        this.driveFileId = driveFileId;
        this.driveFolderId = driveFolderId;
        this.lead = lead;
    }

    public GoogleDriveFile(String driveFileId, String driveFolderId, Contract contract) {
        this.driveFileId = driveFileId;
        this.driveFolderId = driveFolderId;
        this.contract = contract;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDriveFileId() {
        return driveFileId;
    }

    public void setDriveFileId(String driveFileId) {
        this.driveFileId = driveFileId;
    }

    public String getDriveFolderId() {
        return driveFolderId;
    }

    public void setDriveFolderId(String driveFolderId) {
        this.driveFolderId = driveFolderId;
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
