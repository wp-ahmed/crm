package site.easy.to.build.crm.google.model.gmail;

import java.util.List;

public class Payload {
    private String mimeType;
    private List<CustomHeader> headers;
    private Body body;
    private List<Part> parts;

    // Getters and setters

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public List<CustomHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<CustomHeader> headers) {
        this.headers = headers;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public List<Part> getParts() {
        return parts;
    }

    public void setParts(List<Part> parts) {
        this.parts = parts;
    }
}