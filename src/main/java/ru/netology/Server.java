package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    ServerSocket serverSocket;
    ExecutorService executor;
    HashMap<String, Handler> handlers;

    public Server() throws IOException {
        executor = Executors.newFixedThreadPool(64);
        handlers = new HashMap<>();
    }

    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method + " " + path)) {
            handlers.put(method + " " + path, handler);
        }
    }

    public void startServer() throws IOException {
        try (final var serverSocket = new ServerSocket(9999)) {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                executor.execute(() -> proceedConnection(socket));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
    }

    private void proceedConnection(Socket socket) {
        try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             final var out = new BufferedOutputStream(socket.getOutputStream())) {

            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();

            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                socket.close();
                return;
            }

            String method = parts[0];
            final var path = parts[1];
            Request request = null;
            if (method != null && !method.isBlank()) {
                request = new Request(method, path);
            }


            if (request == null || !handlers.containsKey(request.getMethod() + " " + request.getHeaders())) {
                badRequest(out, "400", "Bad Request");
                return;
            }

            String requestPath = request.getMethod() + " " + request.getHeaders();


            if (handlers.containsKey(requestPath)) {
                Handler handler = handlers.get(requestPath);
                handler.handle(request, out);
            } else {
                badRequest(out, "404", "Not Found");

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void goodRequest(BufferedOutputStream out, String path) throws IOException {
        final var filePath = Path.of(".", "public", path);
        final var mimeType = Files.probeContentType(filePath);

        // special case for classic
        if (path.equals("/classic.html")) {
            final var template = Files.readString(filePath);
            final var content = template.replace(
                    "{time}",
                    LocalDateTime.now().toString()
            ).getBytes();
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + content.length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.write(content);
            out.flush();
            return;
        }

        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }

    void badRequest(BufferedOutputStream out, String responseCode, String responseStatus) throws IOException {
        out.write((
                "HTTP/1.1 " + responseCode + " " + responseStatus + "\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }



}
