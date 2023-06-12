package site.easy.to.build.crm.service;

import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;

import java.security.Principal;

public interface OAuthUserService {

    OAuthUser findById(int id);

    void save(OAuthUser oAuthUser, User user);

    void deleteById(int id);
}
