package site.easy.to.build.crm.service.user;

import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;


public interface OAuthUserService {

    public OAuthUser findById(int id);

    public OAuthUser findBtEmail(String email);

    public OAuthUser getOAuthUserByUser(User user);

    public String refreshAccessTokenIfNeeded(OAuthUser oauthUser);

    public void revokeAccess(OAuthUser oAuthUser);

    public void save(OAuthUser oAuthUser, User user);

    public void save(OAuthUser oAuthUser);

    public void deleteById(int id);

    public void updateOAuthUserTokens(OAuthUser oAuthUser, OAuth2AccessToken oAuth2AccessToken, OAuth2RefreshToken oAuth2RefreshToken);


}
