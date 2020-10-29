import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String PATH1 = "/somefile1.html";
    public static final String PATH2 = "/somefile2.png";
    public static final String PATH3 = "/somefile3.png";

    private final int readAheadLimit = 4096;

    private List<String> validPaths;
    private List<String> allowedMethods;

    private ServerSocket server;
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private ConcurrentMap<String, Handler> pathMap;
    private ConcurrentMap<String, Map<String, Handler>> handlers;

    public Server() {
        pathMap = new ConcurrentHashMap<>();
        handlers = new ConcurrentHashMap<>();
        validPaths = List.of(PATH1, PATH2, PATH3);
        allowedMethods = List.of(GET, POST);
    }

    public void listen(int port) {
        try {
            server = new ServerSocket(port);
            while (true) {
                Socket socket = server.accept();
                Connection connection = new Connection(socket);
                executorService.submit(connection);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        pathMap.put(path, handler);
        handlers.put(method, pathMap);
    }

    private void runHandler(Request request, BufferedOutputStream out) throws IOException {
        handlers.get(request.getMethod()).get(request.getPath()).handle(request, out);
    }

    private class Connection implements Runnable {
        private Socket socket;
        private BufferedInputStream in;
        private BufferedOutputStream out;

        public Connection(Socket socket) throws IOException {
            this.socket = socket;
            try {
                in = new BufferedInputStream(socket.getInputStream());
                out = new BufferedOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                close();
            }
        }

        private void close() {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private Request requestParser(BufferedInputStream in) throws IOException {

            in.mark(readAheadLimit);
            byte[] buffer = new byte[readAheadLimit];
            int read = in.read(buffer);

            //requestLine
            byte[] requestLineDelimiter = new byte[]{'\r', '\n'};
            int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);

            if (requestLineEnd == -1) {
                badRequest(out);
            }

            String[] requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
            if (requestLine.length != 3) {
                badRequest(out);
            }

            String method = requestLine[0];
            if (!allowedMethods.contains(method)) {
                badRequest(out);
            }

            String path = requestLine[1];
            if (!path.startsWith("/")) {
                badRequest(out);
            }

            //headers
            byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
            int headersStart = requestLineEnd + requestLineDelimiter.length;
            int headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);

            if (headersEnd == -1) {
                badRequest(out);
            }

            in.reset();
            in.skip(headersStart);

            byte[] headersBytes = in.readNBytes(headersEnd - headersStart);
            List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));

            //messageBody
            String body = "";
            if (!method.equals(GET)) {
                in.skip(headersDelimiter.length);

                Optional<String> contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    int length = Integer.parseInt(contentLength.get());
                    byte[] bodyBytes = in.readNBytes(length);
                    body = new String(bodyBytes);
                    System.out.println(body);
                }
            }

            return new Request(method, path, headers, body);
        }


        private Optional<String> extractHeader(List<String> headers, String header) {
            return headers.stream()
                    .filter(o -> o.startsWith(header))
                    .map(o -> o.substring(o.indexOf(" ")))
                    .map(String::trim)
                    .findFirst();
        }

        private void badRequest(BufferedOutputStream out) throws IOException {
            out.write((
                    "HTTP/1.1 400 Bad Request\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        }

        private int indexOf(byte[] array, byte[] target, int start, int max) {
            outer:
            for (int i = start; i < max - target.length + 1; i++) {
                for (int j = 0; j < target.length; j++) {
                    if (array[i + j] != target[j]) {
                        continue outer;
                    }
                }
                return i;
            }
            return -1;
        }

        private void badrequest() {
            // bad request notify
        }

        private boolean requestValidation(Request request) {
            // checking validRequest
            // checking validPath
            return false;
        }


        @Override
        public void run() {
            try {
                Request request = requestParser(in);

                if (!requestValidation(request)) {
                    socket.close();
                }

                runHandler(request, out);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
        }
    }


    public void setValidPath(String validPath) {
        validPaths.add(validPath);
    }

    public void removeValidPath(String validPath) {
        validPaths.remove(validPath);
    }
}