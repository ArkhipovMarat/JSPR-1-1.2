import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private List<String> validPaths = List.of("/somefile1.html", "/somefile2.png", "/somefile3.png");
    private ServerSocket server;
    private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private ConcurrentMap<String, Map<String, Handler>> handlers;

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
        Map<String,Handler> pathMap = new HashMap<>();
        pathMap.put(path,handler);
        handlers.put(method, pathMap);
    }

    private class Connection implements Runnable {
        private Socket socket;
        BufferedReader in;
        BufferedOutputStream out;

        public Connection(Socket socket) throws IOException {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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

        @Override
        public void run() {
            try {
                final var requestLine = in.readLine();

                Request request = requestParser(requestLine);

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

    private void runHandler(Request request, BufferedOutputStream out) throws IOException {
        handlers.get(request.getMethod()).get(request.getPath()).handle(request, out);
    }

    private boolean requestValidation(Request request) {
        // checking validRequest
        // checking validPath

        // TODO
        return false;
    }

    public Request requestParser(String requestLine) {
        String startingLine = null;
        String message_body = null;
        String headers = null;
        // TODO
        return new Request(startingLine, headers, message_body);
    }

    public void setValidPath(String validPath) {
        validPaths.add(validPath);
    }

    public void removeValidPath(String validPath) {
        validPaths.remove(validPath);
    }
}