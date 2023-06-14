package site.easy.to.build.crm.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.dao.OAuthUserRepository;
import site.easy.to.build.crm.dao.UserRepository;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;

import java.io.IOException;
import java.time.Instant;

@Service
public class OAuthUserServiceImpl implements OAuthUserService{

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Autowired
    OAuthUserRepository oAuthUserRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public OAuthUser findById(int id) {
        return oAuthUserRepository.findById(id);
    }

    @Override
    public OAuthUser getOAuthUserByUser(User user) {
        return oAuthUserRepository.getOAuthUserByUser(user);
    }

    @Override
    public String refreshAccessTokenIfNeeded(OAuthUser oauthUser) {
        Instant now = Instant.now();
        if (now.isBefore(oauthUser.getAccessTokenExpiration())) {
            return oauthUser.getAccessToken();
        }

        GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        // Create a new GoogleTokenResponse
        GoogleTokenResponse tokenResponse;
        try {
            tokenResponse = new GoogleRefreshTokenRequest(
                    new NetHttpTransport(),
                    jsonFactory,
                    oauthUser.getRefreshToken(),
                    clientId,
                    clientSecret)
                    .execute();
            String newAccessToken = tokenResponse.getAccessToken();
            long expiresIn = tokenResponse.getExpiresInSeconds();
            Instant expiresAt = Instant.now().plusSeconds(expiresIn);

            oauthUser.setAccessToken(newAccessToken);
            oauthUser.setAccessTokenExpiration(expiresAt);

            oAuthUserRepository.save(oauthUser);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return oauthUser.getAccessToken();
    }

    @Override
    public void save(OAuthUser oAuthUser, User user) {
        oAuthUser.setUser(user);
        user.setOauthUser(oAuthUser);
        oAuthUserRepository.save(oAuthUser);
    }

    @Override
    public void deleteById(int id) {

    }

}
