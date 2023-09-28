package site.easy.to.build.crm.google.service.drive;

import com.google.api.client.http.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.File;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.model.drive.GoogleDriveFile;
import site.easy.to.build.crm.google.model.drive.GoogleDriveFolder;
import site.easy.to.build.crm.google.util.GoogleApiHelper;
import site.easy.to.build.crm.service.user.OAuthUserService;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class  GoogleDriveApiServiceImpl implements GoogleDriveApiService {

    private static final String API_BASE_URL = "https://www.googleapis.com/drive/v3/files";

    private final OAuthUserService oAuthUserService;

    public GoogleDriveApiServiceImpl(OAuthUserService oAuthUserService) {
        this.oAuthUserService = oAuthUserService;
    }

    @Override
    public List<GoogleDriveFile> listFiles(OAuthUser oAuthUser) throws IOException, GeneralSecurityException {

        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        HashMap<String,String> queryParams = new HashMap<>();
        queryParams.put("q", "mimeType != 'application/vnd.google-apps.folder'");
        queryParams.put("fields","nextPageToken,files(id,name,mimeType,webViewLink,createdTime)");

        GenericUrl driveUrl = GoogleApiHelper.buildGenericUrl(API_BASE_URL,queryParams);
        HttpRequest request = httpRequestFactory.buildGetRequest(driveUrl);
        HttpResponse response = request.execute();
        String respondBody = response.parseAsString();
        Gson gson = new Gson();
        JsonObject jsonResponse = gson.fromJson(respondBody, JsonObject.class);
        JsonArray filesArray = jsonResponse.getAsJsonArray("files");

        Type fileListType = new TypeToken<List<GoogleDriveFile>>() {}.getType();

        return gson.fromJson(filesArray, fileListType);

    }

    @Override
    public List<GoogleDriveFile> listFilesInFolder(OAuthUser oAuthUser, String folderId) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "'" + folderId + "' in parents");
        queryParams.put("fields", "nextPageToken, files(id, name, mimeType, webViewLink, createdTime)");

        GenericUrl driveUrl = GoogleApiHelper.buildGenericUrl(API_BASE_URL, queryParams);
        HttpRequest request = httpRequestFactory.buildGetRequest(driveUrl);
        HttpResponse response = request.execute();
        String responseBody = response.parseAsString();
        Gson gson = new Gson();
        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
        JsonArray filesArray = jsonResponse.getAsJsonArray("files");

        Type fileListType = new TypeToken<List<GoogleDriveFile>>() {}.getType();

        return gson.fromJson(filesArray, fileListType);
    }

    @Override
    public void createWorkspaceFile(OAuthUser oAuthUser, String name, String type) throws IOException, GeneralSecurityException {
        String mimeType = getMimeTypeForType(type);

        JsonObject fileMetadata = new JsonObject();
        fileMetadata.addProperty("name", name);
        fileMetadata.addProperty("mimeType", mimeType);

        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        HashMap<String,String> queryParams = new HashMap<>();
        queryParams.put("fields","id,name,mimeType,webViewLink,createdTime");

        GenericUrl driveUrl = GoogleApiHelper.buildGenericUrl(API_BASE_URL,queryParams);

        HttpContent httpContent = ByteArrayContent.fromString("application/json", fileMetadata.toString());

        HttpRequest request = httpRequestFactory.buildPostRequest(driveUrl, httpContent);

        request.execute();
    }

    @Override
    public List<String> uploadWorkspaceFile(OAuthUser oAuthUser, List<File> files, String folderId) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);
        List<String> fileIds = new ArrayList<>();
        for (File file : files) {
            String name = file.getFileName();
            String mimeType = file.getFileType();
            if (name == null || name.isEmpty()) {
                System.out.println("Skipping file with empty name.");
                continue;
            }

            byte[] fileData = file.getFileData();
            ByteArrayContent content = new ByteArrayContent(mimeType, fileData);
            com.google.api.client.http.HttpRequest request = httpRequestFactory.buildPostRequest(
                    new GenericUrl("https://www.googleapis.com/upload/drive/v3/files"),
                    content
            );

            request.getHeaders().set("Authorization", "Bearer " + accessToken);
            request.getHeaders().set("Content-Disposition", "attachment; filename=\"" + name + "\"");

            JsonObject metadataJson = new JsonObject();
            metadataJson.addProperty("name", name);
            metadataJson.addProperty("mimeType", mimeType);
            JsonArray parentsArray = new JsonArray();

            if (!folderId.isEmpty()) {
                parentsArray.add(folderId);
                metadataJson.add("parents", parentsArray);
            }

            String metadataString = metadataJson.toString();
            ByteArrayContent metadataContent = ByteArrayContent.fromString("application/json", metadataString);

            metadataContent.setType(String.valueOf(new HttpMediaType("application/json")));

            MultipartContent multipartContent = new MultipartContent();
            multipartContent.addPart(new MultipartContent.Part(metadataContent));
            multipartContent.addPart(new MultipartContent.Part(new ByteArrayContent(mimeType, fileData)));

            request.setContent(multipartContent);
//            request.getHeaders().set("name", name);
            HttpResponse response = request.execute();
            JsonObject jsonResponse = JsonParser.parseString(response.parseAsString()).getAsJsonObject();
            String fileId = jsonResponse.get("id").getAsString();
            fileIds.add(fileId);
        }
        return fileIds;
    }

    @Override
    public String createFolder(OAuthUser oAuthUser, String folderName) throws IOException, GeneralSecurityException {
        JsonObject folderMetadata = new JsonObject();
        folderMetadata.addProperty("name", folderName);
        folderMetadata.addProperty("mimeType", "application/vnd.google-apps.folder");

        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        ByteArrayContent content = ByteArrayContent.fromString("application/json", folderMetadata.toString());

        GenericUrl driveUrl = GoogleApiHelper.buildGenericUrl(API_BASE_URL,null);

        HttpRequest request = httpRequestFactory.buildPostRequest(driveUrl, content);
        HttpResponse response = request.execute();

        JsonObject jsonResponse = JsonParser.parseString(response.parseAsString()).getAsJsonObject();
        return jsonResponse.get("id").getAsString();
    }

    @Override
    public void checkFolderExists(OAuthUser oAuthUser, String folderId) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        GenericUrl driveUrl = new GenericUrl(API_BASE_URL + "/" + folderId);

        HttpRequest request = httpRequestFactory.buildGetRequest(driveUrl);
        HttpResponse response = request.execute();

    }
    @Override
    public void createFileInFolder(OAuthUser oAuthUser, String fileName, String folderId, String type) throws IOException, GeneralSecurityException {
        String mimeType = getMimeTypeForType(type);
        JsonObject fileMetadata = new JsonObject();
        fileMetadata.addProperty("name", fileName);
        fileMetadata.addProperty("mimeType", mimeType);
        if(!folderId.isEmpty()){
            JsonArray parentsArray = new JsonArray();
            parentsArray.add(folderId);
            fileMetadata.add("parents", parentsArray);
        }

        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        ByteArrayContent content = ByteArrayContent.fromString("application/json", fileMetadata.toString());


        HashMap<String,String> queryParams = new HashMap<>();
        queryParams.put("fields","id,name,mimeType,webViewLink,createdTime");

        GenericUrl driveUrl = GoogleApiHelper.buildGenericUrl(API_BASE_URL,queryParams);

        HttpRequest request = httpRequestFactory.buildPostRequest(driveUrl, content);
        request.execute();
    }
    public void findOrCreateTemplateFolder(OAuthUser oAuthUser, String folderName) throws IOException, GeneralSecurityException {
        List<GoogleDriveFolder> folders = listFolders(oAuthUser);
        for (GoogleDriveFolder folder : folders) {
            if (folder.getName().equals(folderName)) {
                return;
            }
        }
        createFolder(oAuthUser,folderName);
    }
    public List<GoogleDriveFolder> listFolders(OAuthUser oAuthUser) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        HashMap<String,String> queryParams = new HashMap<>();
        queryParams.put("fields", "nextPageToken,files(id,name,mimeType,webViewLink,createdTime)");
        queryParams.put("q", "mimeType='application/vnd.google-apps.folder' and trashed = false");

        GenericUrl driveUrl = GoogleApiHelper.buildGenericUrl(API_BASE_URL,queryParams);


        HttpRequest request = httpRequestFactory.buildGetRequest(driveUrl);
        HttpResponse response = request.execute();
        String respondBody = response.parseAsString();
        Gson gson = new Gson();
        JsonObject jsonResponse = gson.fromJson(respondBody, JsonObject.class);
        JsonArray filesArray = jsonResponse.getAsJsonArray("files");

        Type fileListType = new TypeToken<List<GoogleDriveFolder>>() {}.getType();

        return gson.fromJson(filesArray, fileListType);
    }

    public void shareFileWithUser(OAuthUser oAuthUser, String fileId, String email, String role) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        // Create the JSON payload for the POST request
        JsonObject permission = new JsonObject();
        permission.addProperty("type", "user");
        permission.addProperty("role", role);
        permission.addProperty("emailAddress", email);

        HttpContent httpContent = ByteArrayContent.fromString("application/json", permission.toString());

        // Build the URL for creating permissions
        String permissionsUrl = API_BASE_URL + "/" + fileId + "/permissions?sendNotificationEmail=true";
        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("fields", "id");
        GenericUrl driveUrl = GoogleApiHelper.buildGenericUrl(permissionsUrl, queryParams);

        // Create and execute the POST request
        HttpRequest request = httpRequestFactory.buildPostRequest(driveUrl, httpContent);
        request.execute();
    }

    private String getMimeTypeForType(String type) {
        return switch (type) {
            case "doc" -> "application/vnd.google-apps.document";
            case "sheet" -> "application/vnd.google-apps.spreadsheet";
            case "slide" -> "application/vnd.google-apps.presentation";
            default -> throw new IllegalArgumentException("Invalid document type");
        };
    }

    @Override
    public void deleteFile(OAuthUser oAuthUser, String fileId) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        // Build the URL for deleting the file
        String deleteUrl = API_BASE_URL + "/" + fileId;
        GenericUrl driveUrl = new GenericUrl(deleteUrl);

        // Create and execute the DELETE request
        HttpRequest request = httpRequestFactory.buildDeleteRequest(driveUrl);
        request.execute();
    }

    @Override
    public boolean isFileExists(OAuthUser oAuthUser, String fileId) throws IOException, GeneralSecurityException {

        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        HashMap<String, String> queryParams = new HashMap<>();
        queryParams.put("q", "mimeType != 'application/vnd.google-apps.folder'");
        queryParams.put("fields", "files(id)");

        GenericUrl driveUrl = GoogleApiHelper.buildGenericUrl(API_BASE_URL, queryParams);
        HttpRequest request = httpRequestFactory.buildGetRequest(driveUrl);
        HttpResponse response = request.execute();
        String responseBody = response.parseAsString();
        Gson gson = new Gson();
        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
        JsonArray filesArray = jsonResponse.getAsJsonArray("files");

        for (JsonElement fileElement : filesArray) {
            JsonObject fileObject = fileElement.getAsJsonObject();
            String existingFileId = fileObject.get("id").getAsString();
            if (existingFileId.equals(fileId)) {
                return true; // File with the provided ID exists in Google Drive
            }
        }

        return false; // File with the provided ID does not exist in Google Drive
    }
}
