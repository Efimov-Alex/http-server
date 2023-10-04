package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Server server = new Server();

        server.addHandler("POST", "/messages", (request, responseStream) -> server.badRequest(responseStream, "503", "Service Unavailable"));

        server.addHandler("GET", "/", ((request, outputStream) -> server.goodRequest(outputStream, "index.html")));

        server.addHandler("GET", "/png", ((request, outputStream) -> server.goodRequest(outputStream, "spring.png")));

        server.startServer();

    }
}


