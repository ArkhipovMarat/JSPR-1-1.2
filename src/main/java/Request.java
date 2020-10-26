import java.util.List;

public class Request {
    private String startingLine;
    private String headers;
    private String message_body;

    public Request(String startingLine, String headers, String message_body) {
        this.startingLine = startingLine;
        this.headers = headers;
        this.message_body = message_body;
    }

    public String getStartingLine() {
        return startingLine;
    }

    public String getHeaders() {
        return headers;
    }

    public String getMessage_body() {
        return message_body;
    }

    public String getMethod() {
        final var parts = startingLine.split(" ");
        if (parts.length != 3) {
            return "";
        }
        return parts[0];
    }

    public String getPath() {
        final var parts = startingLine.split(" ");
        if (parts.length != 3) {
            return "";
        }
        return parts[1];
    }
}