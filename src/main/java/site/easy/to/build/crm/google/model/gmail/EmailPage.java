package site.easy.to.build.crm.google.model.gmail;

import java.util.List;

public class EmailPage {
    private List<GmailEmailInfo> emails;
    private String nextPageToken;
    private int page;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public List<GmailEmailInfo> getEmails() {
        return emails;
    }

    public void setEmails(List<GmailEmailInfo> emails) {
        this.emails = emails;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }
}