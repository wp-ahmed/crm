package site.easy.to.build.crm.util;


import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.service.gmail.GoogleGmailApiService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

public class EmailTokenUtils {

    public static void sendRegistrationEmail(String email, String name, String passwordLinkSetter,
                                             OAuthUser oAuthUser, GoogleGmailApiService googleGmailApiService) {


        // Create the email content with a link that includes the token
        String body = "Dear " + name + ",\n\n" +
                "Welcome to our application! Please click the following link to set your password:\n\n" +
                passwordLinkSetter;

        try {
            googleGmailApiService.sendEmail(oAuthUser, email, "Account Registration", body);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

}
