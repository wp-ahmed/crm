package site.easy.to.build.crm.google.service.acess;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.view.RedirectView;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.entity.User;

import java.io.IOException;
import java.util.List;


public interface GoogleAccessService {


    static final String SCOPE_CALENDAR = "https://www.googleapis.com/auth/calendar";
    static final String SCOPE_GMAIL = "https://www.googleapis.com/auth/gmail.modify";
    static final String SCOPE_DRIVE = "https://www.googleapis.com/auth/drive.file";
    static final String REDIRECT_URI = "employee/settings/handle-granted-access";

    public RedirectView grantGoogleAccess(Authentication authentication,
                                          HttpSession session,
                                          boolean grantCalendarAccess,
                                          boolean grantGmailAccess,
                                          boolean grantDriveAccess,
                                          HttpServletRequest request);
    public String handleGrantedAccess(HttpSession session,
                                      String error,
                                      String authCode,
                                      String state,
                                      Authentication authentication,
                                      HttpServletRequest request) throws IOException;

    public void verifyAccessAndHandleRevokedToken(OAuthUser oAuthUser, User user, List<String> scopesToCheck) throws IOException;

}
