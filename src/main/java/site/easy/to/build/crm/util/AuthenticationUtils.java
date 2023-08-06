package site.easy.to.build.crm.util;


import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Component;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.OAuthUserService;
import site.easy.to.build.crm.service.UserService;

@Component
public class AuthenticationUtils {

    private final UserService userService;
    private final OAuthUserService oAuthUserService;

    public AuthenticationUtils(UserService userService, OAuthUserService oAuthUserService) {
        this.userService = userService;
        this.oAuthUserService = oAuthUserService;
    }

    public OAuthUser getOAuthUserFromAuthentication(Authentication authentication) {
        String email = ((DefaultOidcUser) authentication.getPrincipal()).getEmail();
        User user = userService.findByEmail(email);
        return oAuthUserService.getOAuthUserByUser(user);
    }
}