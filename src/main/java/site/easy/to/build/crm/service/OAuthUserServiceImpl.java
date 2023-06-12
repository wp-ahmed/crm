package site.easy.to.build.crm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.easy.to.build.crm.dao.OAuthUserRepository;
import site.easy.to.build.crm.dao.UserRepository;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;

import java.security.Principal;

@Service
public class OAuthUserServiceImpl implements OAuthUserService{

    @Autowired
    OAuthUserRepository oAuthUserRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public OAuthUser findById(int id) {
        return oAuthUserRepository.findById(id);
    }

    @Override
    public void save(OAuthUser oAuthUser, User user) {
        oAuthUser.setUser(user);
        user.setOauthUser(oAuthUser);
//        userRepository
        oAuthUserRepository.save(oAuthUser);
    }

    @Override
    public void deleteById(int id) {

    }
}
