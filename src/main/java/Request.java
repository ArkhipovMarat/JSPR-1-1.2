import java.util.List;

public class Request {
    private String method;
    private String path;
    private List<String> headers;
    private String messageBody;

    public Request(String method, String path, List<String> headers, String messageBody) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.messageBody = messageBody;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public String getMessageBody() {
        return messageBody;
    }
}