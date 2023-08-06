package site.easy.to.build.crm.config.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.OAuthUserService;
import site.easy.to.build.crm.service.UserService;

import java.io.IOException;

@Component
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    public OAuthUserService oAuthUserService;

    @Autowired
    public UserService userService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // Get the registration ID of the OAuth2 provider
        DefaultOidcUser oidcUser = (DefaultOidcUser) authentication.getPrincipal();

        // Get the registration ID of the OAuth2 provider
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        if (registrationId == null) {
            // Handle the case when the registrationId is not found
            throw new ServletException("Failed to find the registrationId from the authorities");
        }



        // Obtain the OAuth2AuthorizedClient
        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(registrationId, authentication.getName());



        // Get the access and the refresh token from the OAuth2AuthorizedClient

        OAuth2AccessToken oAuth2AccessToken = authorizedClient.getAccessToken();
        OAuth2RefreshToken oAuth2RefreshToken = authorizedClient.getRefreshToken();

        String email = ((DefaultOidcUser) authentication.getPrincipal()).getEmail();
        String name = email.split("@")[0];
        String username = email.split("@")[0];

        User user = userService.findByEmail(email);
        OAuthUser oAuthUser;

        if(user == null) {
            user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setFirstName(name);
            user.setStatus(User.Status.ACTIVE);
            user.setDepartment("client");
            userService.save(user);
            oAuthUser = new OAuthUser();
            oAuthUser.getGrantedScopes().add("openid");
            oAuthUser.getGrantedScopes().add("email");
            oAuthUser.getGrantedScopes().add("profile");
            oAuthUserService.updateOAuthUserTokens(oAuthUser, oAuth2AccessToken, oAuth2RefreshToken);
        }else{
            oAuthUser = user.getOauthUser();
        }

        oAuthUserService.save(oAuthUser,user);

        response.sendRedirect("/settings/google-services");
    }
}