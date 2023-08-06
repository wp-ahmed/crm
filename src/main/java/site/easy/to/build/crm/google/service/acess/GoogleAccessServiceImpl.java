package site.easy.to.build.crm.google.service.acess;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.google.config.GoogleApiProperties;
import site.easy.to.build.crm.google.config.GoogleAuthorizationCodeFlowWrapper;
import site.easy.to.build.crm.google.service.drive.GoogleDriveApiService;
import site.easy.to.build.crm.service.OAuthUserService;
import site.easy.to.build.crm.service.UserService;
import site.easy.to.build.crm.util.SessionUtils;
import site.easy.to.build.crm.google.util.StringSetWrapper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.*;

@Service
public class GoogleAccessServiceImpl implements GoogleAccessService {

    private final UserService userService;
    private final OAuthUserService oAuthUserService;
    private final GoogleApiProperties googleApiProperties;

    private final GoogleAuthorizationCodeFlowWrapper googleAuthorizationCodeFlowWrapper;

    private final GoogleDriveApiService googleDriveApiService;

    public GoogleAccessServiceImpl(UserService userService, OAuthUserService oAuthUserService, GoogleApiProperties googleApiProperties, GoogleAuthorizationCodeFlowWrapper googleAuthorizationCodeFlowWrapper, GoogleDriveApiService googleDriveApiService) {
        this.userService = userService;
        this.oAuthUserService = oAuthUserService;
        this.googleApiProperties = googleApiProperties;
        this.googleAuthorizationCodeFlowWrapper = googleAuthorizationCodeFlowWrapper;
        this.googleDriveApiService = googleDriveApiService;
    }

    @Override
    public RedirectView grantGoogleAccess(Authentication authentication, HttpSession session, boolean grantCalendarAccess, boolean grantGmailAccess, boolean grantDriveAccess) {
        String state = "YOUR_STATE_VALUE";
        String accessType = "offline";

        String email = ((DefaultOidcUser) authentication.getPrincipal()).getEmail();

        Set<String> newGrantedScopes = new HashSet<>();
        updateAccess(newGrantedScopes, grantCalendarAccess, SCOPE_CALENDAR);
        updateAccess(newGrantedScopes, grantGmailAccess, SCOPE_GMAIL);
        updateAccess(newGrantedScopes,grantDriveAccess,SCOPE_DRIVE);

        session.setAttribute("updatedScopes", new StringSetWrapper(newGrantedScopes));
        String[] scopes = googleApiProperties.getScope().split(",");
        Set<String> SetOfRequiredScopes = new HashSet<>(Arrays.asList(scopes));
        SetOfRequiredScopes.addAll(newGrantedScopes);

        List<String> requiredScopes = new ArrayList<>(SetOfRequiredScopes);
        GoogleAuthorizationCodeFlow authorizationCodeFlow = googleAuthorizationCodeFlowWrapper.build(requiredScopes);
        String authorizationRequestUrl = googleApiProperties.buildAuthorizationUri(REDIRECT_URI,state,accessType,email,requiredScopes,authorizationCodeFlow);

        return new RedirectView(authorizationRequestUrl);
    }

    @Override
    public String handleGrantedAccess(HttpSession session, String error, String authCode, String state, Authentication authentication) throws IOException {

        if ("access_denied".equals(error)) {
            // The user has canceled the consent page, so update the granted scopes
            return "redirect:/register";
        }

        String email = ((DefaultOidcUser) authentication.getPrincipal()).getEmail();
        User user = userService.findByEmail(email);
        OAuthUser oAuthUser = user.getOauthUser();

        handleScopeChanges(session,oAuthUser);
        List<String> requiredScopes = new ArrayList<>(oAuthUser.getGrantedScopes());

        GoogleAuthorizationCodeFlow flow = googleAuthorizationCodeFlowWrapper.build(requiredScopes);


        // Exchange the authorization code for an access token and a refresh token.
        GoogleTokenResponse tokenResponse = flow.newTokenRequest(authCode).setRedirectUri(REDIRECT_URI).execute();

        // Obtain the OAuthUser object that represents the authenticated user. This assumes you have a method to retrieve the OAuthUser.

        // Update the OAuthUser object with the new access token, refresh token, and access token expiration.
        oAuthUser.setAccessToken(tokenResponse.getAccessToken());
        oAuthUser.setRefreshToken(tokenResponse.getRefreshToken());
        oAuthUser.setAccessTokenExpiration(Instant.ofEpochMilli(tokenResponse.getExpiresInSeconds() * 1000L));

        oAuthUserService.refreshAccessTokenIfNeeded(oAuthUser);

        Set<String> actualGrantedScopes = extractActualGrantedScopes(tokenResponse);
        oAuthUser.setGrantedScopes(actualGrantedScopes);
        oAuthUserService.save(oAuthUser,user);
        if(actualGrantedScopes.contains(SCOPE_DRIVE)){
            try {
                googleDriveApiService.findOrCreateTemplateFolder(oAuthUser,"Templates");
            } catch (IOException | GeneralSecurityException e) {
//                throw new RuntimeException(e);
            }
        }


        return "redirect:/settings/google-services";
    }

    public void verifyAccessAndHandleRevokedToken(OAuthUser oAuthUser, User user, List<String> scopesToCheck) throws IOException {
        try {
            // Request a new access token using the refresh token
            HttpTransport httpTransport = new NetHttpTransport();
            GsonFactory jsonFactory = new GsonFactory();

            GoogleRefreshTokenRequest refreshTokenRequest = new GoogleRefreshTokenRequest(
                    httpTransport,
                    jsonFactory,
                    oAuthUser.getRefreshToken(),
                    googleApiProperties.getClientId(),
                    googleApiProperties.getClientSecret()
            );
            GoogleTokenResponse tokenResponse = refreshTokenRequest.execute();
            String newAccessToken = tokenResponse.getAccessToken();

            HttpRequestFactory requestFactory = httpTransport.createRequestFactory(request -> request.setParser(new JsonObjectParser(jsonFactory)));
            GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v3/tokeninfo");
            url.set("access_token", newAccessToken);

            HttpRequest request = requestFactory.buildGetRequest(url);
            HttpResponse response = request.execute();

        } catch (TokenResponseException e) {
            if (e.getDetails() != null && "invalid_grant".equals(e.getDetails().getError())) {
                // Handle the specific case when the token has been expired or revoked
                String[] scopes = googleApiProperties.getScope().split(",");
                Set<String> setOfRequiredScopes = new HashSet<>(Arrays.asList(scopes));
                oAuthUser.setGrantedScopes(setOfRequiredScopes);
                oAuthUserService.save(oAuthUser, user);
            } else {
                // Log and handle other exceptions as needed
            }
            return;
        } catch (IOException e) {
            // Log and handle other IOExceptions as needed
            return;
        }
    }

    private void updateAccess(Set<String> grantedScopes, boolean grantAccess, String scope) {
        if (grantAccess) {
            grantedScopes.add(scope);
        } else {
            grantedScopes.remove(scope);
        }
    }

    private void handleScopeChanges(HttpSession session, OAuthUser oAuthUser) {
        StringSetWrapper stringSetWrapper = SessionUtils.getSessionAttribute(session, "updatedScopes", StringSetWrapper.class);
        Set<String> scopesToRemove = new HashSet<>();
        Set<String> scopesToAdd = new HashSet<>();
        boolean scopeChanges = false;
        String[] scopes = googleApiProperties.getScope().split(",");
        Set<String> grantedScopes = new HashSet<>(Arrays.asList(scopes));

        if (stringSetWrapper != null) {
            Set<String> updatedScopes = stringSetWrapper.getStringSet();
            for (String scope : updatedScopes) {
                if (!oAuthUser.getGrantedScopes().contains(scope)) {
                    scopesToAdd.add(scope);
                    scopeChanges = true;
                }
            }
        }
        for (String scope : oAuthUser.getGrantedScopes()) {
            if (grantedScopes.contains(scope)) {
                continue;
            }
            if (stringSetWrapper == null || !stringSetWrapper.getStringSet().contains(scope)) {
                scopesToRemove.add(scope);
                scopeChanges = true;
            }
        }

        oAuthUser.getGrantedScopes().addAll(scopesToAdd);
        oAuthUser.getGrantedScopes().removeAll(scopesToRemove);

        if (scopeChanges) {
            oAuthUserService.revokeAccess(oAuthUser);
        }
    }
    private Set<String> extractActualGrantedScopes(GoogleTokenResponse tokenResponse) {
        String grantedScopesStr = tokenResponse.get("scope").toString();
        Set<String> actualGrantedScopes = new HashSet<>(Arrays.asList(grantedScopesStr.split(" ")));
        Set<String> userGrantedScopes = new HashSet<>();
        for (String scope : actualGrantedScopes) {
            if (scope.contains("userinfo.")) {
                if (scope.contains("email")) {
                    userGrantedScopes.add("email");
                }
                if (scope.contains("profile")) {
                    userGrantedScopes.add("profile");
                }
            } else {
                userGrantedScopes.add(scope);
            }
        }
        return userGrantedScopes;
    }
}
