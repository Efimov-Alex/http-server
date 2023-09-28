package ru.netology;

public class Request {
    private String method;
    private String headers;

    public Request(String method, String headers) {
        this.method = method;
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public String getHeaders() {
        return headers;
    }
}
