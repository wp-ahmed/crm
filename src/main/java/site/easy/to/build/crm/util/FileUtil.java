package site.easy.to.build.crm.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import site.easy.to.build.crm.entity.File;
import site.easy.to.build.crm.entity.GoogleDriveFile;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.model.gmail.Attachment;
import site.easy.to.build.crm.google.service.drive.GoogleDriveApiService;
import site.easy.to.build.crm.service.drive.GoogleDriveFileService;
import site.easy.to.build.crm.service.file.FileService;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.util.*;

@Component
public class FileUtil {

    private final GoogleDriveApiService googleDriveApiService;
    private final AuthenticationUtils authenticationUtils;
    private final GoogleDriveFileService googleDriveFileService;
    private final FileService fileService;

    @Autowired
    public FileUtil(GoogleDriveApiService googleDriveApiService, AuthenticationUtils authenticationUtils,
                    GoogleDriveFileService googleDriveFileService, FileService fileService) {
        this.googleDriveApiService = googleDriveApiService;
        this.authenticationUtils = authenticationUtils;
        this.googleDriveFileService = googleDriveFileService;
        this.fileService = fileService;
    }

    public List<File> convertAttachmentsToFiles(List<Attachment> attachments) {
        List<File> files = new ArrayList<>();
        for (Attachment attachment : attachments) {
            String attachmentData = attachment.getData();
            String replaceString = attachmentData.replaceAll("_", "/");
            String attachmentDataBase64 = replaceString.replaceAll("-", "+");
            byte[] binaryData = Base64.getDecoder().decode(attachmentDataBase64);
            File file = new File(attachment.getName(), binaryData, attachment.getMimeType());
            files.add(file);
        }
        return files;
    }

    public <T> void saveFiles(List<Attachment> attachments, T entity) {
        List<File> files = new ArrayList<>();
        for (Attachment attachment : attachments) {
            String attachmentData = attachment.getData();
            String replaceString = attachmentData.replaceAll("_", "/");
            String attachmentDataBase64 = replaceString.replaceAll("-", "+");
            byte[] binaryData = Base64.getDecoder().decode(attachmentDataBase64);
            File file = createFile(attachment.getName(), binaryData, attachment.getMimeType(), entity);
            fileService.save(file);
            files.add(file);
        }
        setFiles(entity, files);
    }

    private <T> File createFile(String fileName, byte[] binaryData, String mimeType, T entity) {
        try {
            Class<?> entityClass = entity.getClass();
            Constructor<?> constructor = File.class.getConstructor(String.class, byte[].class, String.class, entityClass);
            return (File) constructor.newInstance(fileName, binaryData, mimeType, entity);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void setFiles(T entity, List<File> files) {
        try {
            Class<?> entityClass = entity.getClass();
            entityClass.getMethod("setFiles", List.class).invoke(entity, files);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void deleteOldFiles(List<File> files, T entity) {
        for (File file : files) {
            fileService.delete(file);
        }
        setFiles(entity, new ArrayList<>());
    }

    public <T> void saveGoogleDriveFiles(Authentication authentication, List<Attachment> allFiles, String folderId, T createdObject) {
        List<GoogleDriveFile> googleDriveFiles = new ArrayList<>();
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        List<File> attachments = convertAttachmentsToFiles(allFiles);
        if(googleDriveFileService != null && googleDriveApiService != null) {
            try {
                List<String> fileIds = googleDriveApiService.uploadWorkspaceFile(oAuthUser, attachments, folderId);
                for (String fileId : fileIds) {
                    GoogleDriveFile googleDriveFile = createGoogleDriveFile(fileId, folderId, createdObject);
                    googleDriveFileService.save(googleDriveFile);
                    googleDriveFiles.add(googleDriveFile);
                }
            } catch (IOException | GeneralSecurityException e) {
                throw new RuntimeException(e);
            }

            setGoogleDriveFiles(createdObject, googleDriveFiles);
        }
    }

    private <T> GoogleDriveFile createGoogleDriveFile(String fileId, String folderId, T createdObject) {
        try {
            Class<?> createdObjectType = createdObject.getClass();
            Constructor<?> constructor = GoogleDriveFile.class.getConstructor(String.class, String.class, createdObjectType);
            return (GoogleDriveFile) constructor.newInstance(fileId, folderId, createdObject);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void setGoogleDriveFiles(T createdObject, List<GoogleDriveFile> googleDriveFiles) {
        try {
            Class<?> createdObjectType = createdObject.getClass();
            createdObjectType.getMethod("setGoogleDriveFiles", List.class).invoke(createdObject, googleDriveFiles);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteGoogleDriveFiles(List<GoogleDriveFile> googleDriveFiles, Authentication authentication) {
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        if(googleDriveFileService != null && googleDriveApiService != null) {
            for (GoogleDriveFile googleDriveFile : googleDriveFiles) {
                googleDriveFileService.delete(googleDriveFile.getId());
                try {
                    if (googleDriveApiService.isFileExists(oAuthUser, googleDriveFile.getDriveFileId())) {
                        googleDriveApiService.deleteFile(oAuthUser, googleDriveFile.getDriveFileId());
                    }
                } catch (IOException | GeneralSecurityException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public <T> void updateFilesBasedOnGoogleDriveFiles(OAuthUser oAuthUser, List<GoogleDriveFile> googleDriveFiles, T object) throws GeneralSecurityException, IOException {
        Class<?> createdObjectType = object.getClass();
        if (googleDriveFiles != null && !googleDriveFiles.isEmpty() && googleDriveFileService != null && googleDriveApiService != null) {
            Iterator<GoogleDriveFile> iterator = googleDriveFiles.iterator();
            while (iterator.hasNext()) {
                GoogleDriveFile googleDriveFile = iterator.next();
                if (!googleDriveApiService.isFileExists(oAuthUser, googleDriveFile.getDriveFileId())) {
                    iterator.remove();
                }
            }
        }
        if(googleDriveFileService != null && googleDriveApiService != null) {
            try {
                createdObjectType.getMethod("setGoogleDriveFiles", List.class).invoke(object, googleDriveFiles);
                if (googleDriveFiles == null || googleDriveFiles.isEmpty()) {
                    createdObjectType.getMethod("setGoogleDrive", Boolean.class).invoke(object, false);
                    createdObjectType.getMethod("setGoogleDriveFolderId", String.class).invoke(object, "");
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
