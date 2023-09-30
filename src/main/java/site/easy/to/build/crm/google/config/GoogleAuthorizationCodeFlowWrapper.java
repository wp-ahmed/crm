package site.easy.to.build.crm.google.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.util.List;

public class GoogleAuthorizationCodeFlowWrapper {

    private final HttpTransport httpTransport;
    private final GsonFactory jsonFactory;
    private final GoogleClientSecrets clientSecrets;


    public GoogleAuthorizationCodeFlowWrapper(String clientId, String clientSecret) {
        httpTransport = new NetHttpTransport();
        jsonFactory = GsonFactory.getDefaultInstance();
        clientSecrets = new GoogleClientSecrets()
                .setInstalled(new GoogleClientSecrets.Details()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret));
    }

    public GoogleAuthorizationCodeFlow build(List<String> scopes) {
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, scopes)
                .setAccessType("offline")
                .build();
    }
}