package site.easy.to.build.crm.google.service.gmail;


import site.easy.to.build.crm.entity.OAuthUser;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface GoogleGmailLabelService {
    List<String> fetchAllLabels(OAuthUser oAuthUser) throws IOException, GeneralSecurityException;

    String createLabel(OAuthUser oAuthUser, String labelName) throws IOException, GeneralSecurityException;

    void deleteLabel(OAuthUser oAuthUser, String labelId) throws IOException, GeneralSecurityException;

    void assignLabelToEmails(OAuthUser oAuthUser, List<String> emailIds, String labelId) throws IOException, GeneralSecurityException;
}