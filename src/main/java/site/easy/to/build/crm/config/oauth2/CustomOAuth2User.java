package site.easy.to.build.crm.config.oauth2;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;


public class CustomOAuth2User implements OAuth2User {
    private OAuth2User oauth2User;

    private DefaultOidcUser defaultOidcUser;
    public CustomOAuth2User(DefaultOidcUser defaultOidcUser) {
        this.defaultOidcUser = defaultOidcUser;
    }

    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return defaultOidcUser.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return defaultOidcUser.getAuthorities();
    }

    @Override
    public String getName() {
        return defaultOidcUser.<String>getAttribute("name");
    }
    public String getEmail() {
        return defaultOidcUser.<String>getAttribute("email");
    }

}
