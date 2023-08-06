package site.easy.to.build.crm.google.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleApiConfig {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Bean
    public GoogleAuthorizationCodeFlowWrapper googleAuthorizationCodeFlowWrapper() {
        return new GoogleAuthorizationCodeFlowWrapper(clientId, clientSecret);
    }
}