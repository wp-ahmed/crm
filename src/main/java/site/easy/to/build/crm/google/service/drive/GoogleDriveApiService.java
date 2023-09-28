package site.easy.to.build.crm.google.service.drive;

import site.easy.to.build.crm.entity.File;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.model.drive.GoogleDriveFile;
import site.easy.to.build.crm.google.model.drive.GoogleDriveFolder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface GoogleDriveApiService {
    public List<GoogleDriveFile> listFiles(OAuthUser oAuthUser) throws IOException, GeneralSecurityException;
    public List<GoogleDriveFile> listFilesInFolder(OAuthUser oAuthUser, String folderId) throws IOException, GeneralSecurityException;
    public void createWorkspaceFile(OAuthUser oAuthUser, String name, String type) throws IOException, GeneralSecurityException;
    public List<String> uploadWorkspaceFile(OAuthUser oAuthUser, List<File> files, String folderId) throws IOException, GeneralSecurityException;

    public String createFolder(OAuthUser oAuthUser, String folderName) throws IOException, GeneralSecurityException;

    void checkFolderExists(OAuthUser oAuthUser, String folderId) throws IOException, GeneralSecurityException;

    public void createFileInFolder(OAuthUser oAuthUser, String fileName, String folderId, String type) throws IOException, GeneralSecurityException;

    public List<GoogleDriveFolder> listFolders(OAuthUser oAuthUser) throws IOException, GeneralSecurityException;

    public void shareFileWithUser(OAuthUser oAuthUser, String fileId, String email, String role) throws IOException, GeneralSecurityException;

    public void findOrCreateTemplateFolder(OAuthUser oAuthUser, String folderName) throws IOException, GeneralSecurityException;

    public void deleteFile(OAuthUser oAuthUser, String fileId) throws IOException, GeneralSecurityException;
    public boolean isFileExists(OAuthUser oAuthUser, String fileId) throws IOException, GeneralSecurityException;
}
