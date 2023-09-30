package site.easy.to.build.crm.google.service.gmail;

import com.google.api.client.http.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.util.GoogleApiHelper;
import site.easy.to.build.crm.google.util.GsonUtil;
import site.easy.to.build.crm.service.user.OAuthUserService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleGmailLabelServiceImpl implements GoogleGmailLabelService{

    private static final String LABELS_API_BASE_URL = "https://www.googleapis.com/gmail/v1/users/me/labels";

    private static final String GMAIL_API_BASE_URL = "https://www.googleapis.com/gmail/v1/users/me";

    private final OAuthUserService oAuthUserService;

    public GoogleGmailLabelServiceImpl(OAuthUserService oAuthUserService) {
        this.oAuthUserService = oAuthUserService;
    }

    @Override
    public List<String> fetchAllLabels(OAuthUser oAuthUser) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        GenericUrl labelsUrl = GoogleApiHelper.buildGenericUrl(LABELS_API_BASE_URL, null);
        JsonObject jsonResponse = executeRequest(httpRequestFactory, labelsUrl);
        JsonArray labelsArray = jsonResponse.getAsJsonArray("labels");

        List<String> labels = new ArrayList<>();
        labelsArray.forEach(labelElement -> {
            JsonObject labelObject = labelElement.getAsJsonObject();
            String labelName = labelObject.get("name").getAsString();
            String labelType = labelObject.get("id").getAsString();
            String type = labelObject.get("type").getAsString();

            // Only add labels with labelType "labelShow" and type "user"
            if (labelType.startsWith("Label")) {
                labels.add(labelName);
            }
        });

        return labels;
    }

    @Override
    public String createLabel(OAuthUser oAuthUser, String labelName) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("name", labelName);

        HttpRequest request = httpRequestFactory.buildPostRequest(
                new GenericUrl(LABELS_API_BASE_URL),
                ByteArrayContent.fromString("application/json", requestBody.toString())
        );

        HttpResponse response = request.execute();
        String responseBody = response.parseAsString();
        JsonObject jsonResponse = GsonUtil.fromJson(responseBody);

        return jsonResponse.get("id").getAsString();
    }

    @Override
    public void deleteLabel(OAuthUser oAuthUser, String labelId) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        GenericUrl deleteUrl = GoogleApiHelper.buildGenericUrl(LABELS_API_BASE_URL + "/" + labelId, null);
        HttpRequest request = httpRequestFactory.buildDeleteRequest(deleteUrl);

        request.execute();
    }

    @Override
    public void assignLabelToEmails(OAuthUser oAuthUser, List<String> emailIds, String labelId) throws IOException, GeneralSecurityException {
        String accessToken = oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);
        HttpRequestFactory httpRequestFactory = GoogleApiHelper.createRequestFactory(accessToken);

        JsonObject requestBody = new JsonObject();
        JsonArray addLabelIdsArray = new JsonArray();
        addLabelIdsArray.add(labelId);
        requestBody.add("addLabelIds", addLabelIdsArray);

        for (String emailId : emailIds) {
            HttpRequest request = httpRequestFactory.buildPostRequest(
                    new GenericUrl(GMAIL_API_BASE_URL + "/messages/" + emailId + "/modify"),
                    ByteArrayContent.fromString("application/json", requestBody.toString())
            );

            request.execute();
        }
    }

    private JsonObject executeRequest(HttpRequestFactory httpRequestFactory, GenericUrl url) throws IOException {
        HttpRequest request = httpRequestFactory.buildGetRequest(url);
        HttpResponse response = request.execute();
        String responseBody = response.parseAsString();
        return GsonUtil.fromJson(responseBody);
    }
}