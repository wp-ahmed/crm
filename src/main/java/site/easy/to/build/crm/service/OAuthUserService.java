package site.easy.to.build.crm.service;

import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;

import java.security.Principal;

public interface OAuthUserService {

    public OAuthUser findById(int id);

    public OAuthUser getOAuthUserByUser(User user);

    public String refreshAccessTokenIfNeeded(OAuthUser oauthUser);

    public void save(OAuthUser oAuthUser, User user);

    public void deleteById(int id);
}
