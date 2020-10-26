import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        final var server = new Server();
        server.addHandler("POST", "/somefile1.html", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream out) throws IOException {
                var filePath = Path.of(".", "someDir", request.getPath());
                var mimeType = Files.probeContentType(filePath);
                var lenth = Files.size(filePath);
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length" + lenth + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                Files.copy(filePath, out);
                out.flush();


                // TODO someAction
            }
        });

        server.addHandler("GET", "/somefile2.html", new Handler() {
            @Override
            public void handle(Request request, BufferedOutputStream out) throws IOException {
                // someAction
            }
        });

        server.listen(8080);
    }
}
