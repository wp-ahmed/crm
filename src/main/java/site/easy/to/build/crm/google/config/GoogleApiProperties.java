package site.easy.to.build.crm.google.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.google")
public class GoogleApiProperties {

    private String clientId;
    private String clientSecret;
    private String scope;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String buildAuthorizationUri(String redirectUri,
                                        String state,
                                        String accessType,
                                        String email,
                                        List<String> requiredScopes,
                                        GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow) {

        return googleAuthorizationCodeFlow
                .newAuthorizationUrl()
                .setClientId(clientId)
                .setRedirectUri(redirectUri)
                .setScopes(requiredScopes)
                .setState(state)
                .setAccessType(accessType)
                .set("login_hint", email)
                .set("prompt", "consent")
                .build();
    }

}