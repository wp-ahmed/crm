package site.easy.to.build.crm.google.model.gmail;

public class Attachment {
    private String name;
    private String data;
    private String mimeType;
    private int size;

    public Attachment() {
    }

    public Attachment(String name, String data, String mimeType, int size) {
        this.name = name;
        this.data = data;
        this.mimeType = mimeType;
        this.size = size;
    }

    public Attachment(String name, String data, String mimeType) {
        this.name = name;
        this.data = data;
        this.mimeType = mimeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
