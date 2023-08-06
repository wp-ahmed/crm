package site.easy.to.build.crm.google.model.gmail;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GmailApiMessage {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("(.*?)<(.*?)>");
    private String id;
    private String threadId;
    private String snippet;
    private long internalDate;
    private List<String> labelIds;
    private Payload payload;

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public long getInternalDate() {
        return internalDate;
    }

    public void setInternalDate(long internalDate) {
        this.internalDate = internalDate;
    }

    public List<String> getLabelIds() {
        return labelIds;
    }

    public void setLabelIds(List<String> labelIds) {
        this.labelIds = labelIds;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    // Utility method to get header value by name
    public String getHeaderValue(String name) {
        if (payload.getHeaders() == null) {
            return null;
        }
        Map<String, String> headerMap = payload.getHeaders().stream()
                .collect(Collectors.toMap(
                        header -> header.getName().toLowerCase(), // Convert the header name to lowercase
                        CustomHeader::getValue,
                        (value1, value2) -> value1 // Keeps the first encountered value
                ));
        return headerMap.getOrDefault(name.toLowerCase(), null); // Convert the input name to lowercase for lookup
    }


    public String[] extractEmailParts(String sender) {
        if(sender == null){
            return new String[]{"", ""};
        }
        Matcher matcher = EMAIL_PATTERN.matcher(sender);
        if (matcher.find()) {
            return new String[]{matcher.group(2).trim(), matcher.group(1).trim()};
        } else {
            return new String[]{sender.trim(), ""};
        }
    }

}
