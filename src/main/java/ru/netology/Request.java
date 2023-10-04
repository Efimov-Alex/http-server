package ru.netology;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;

public class Request {
    private String method;
    private String headers;

    String[] listHeaders;


    private static final String GET = "GET";
    private static final String POST = "POST";

    private List<String> headersParams;
    private List<NameValuePair> params;

    public Request(String method, String headers) {
        this.method = method;
        this.headers = headers;
        listHeaders = headers.split("\\?");


    }

    public Request(String method, String headers, List<String> headersParams, List<NameValuePair> params) {
        this.method = method;
        this.headers = headers;
        this.headersParams = headersParams;
        this.params = params;
    }

    static Request createRequest(BufferedInputStream in) throws IOException, URISyntaxException {
        final List<String> allowedMethods = List.of(GET, POST);
        final var limit = 4096;
        in.mark(limit);
        final var buffer = new byte[limit];
        final var read = in.read(buffer);

        // ищем request line
        final var requestLineDelimiter = new byte[]{'\r', '\n'};
        final var requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            return null;
        }
        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }

        final var method = requestLine[0];
        if (!allowedMethods.contains(method)) {
            return null;
        }

        final var path = requestLine[1];

        // ищем заголовки
        final var headerDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headerDelimiter, headersStart, read);
        if (headersEnd == -1) {
            return null;
        }

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersStart);

        final var headersBytes = in.readNBytes(headersEnd - headersStart);
        List<String> headers = Arrays.asList(new String(headersBytes).split("\r\n"));

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(path), StandardCharsets.UTF_8);

        return new Request(method, path, headers, params);
    }


    public String getMethod() {
        return method;
    }

    public String getHeaders() {
        return listHeaders[0];
    }

    public List<NameValuePair> getQueryParams() {
        if (headers.split("\\?").length > 1) {
            return URLEncodedUtils.parse(headers.split("\\?")[1], Charset.forName("utf-8"));

        }
        return new ArrayList<>();

    }

    public NameValuePair getQueryParam(String name) {
        return getQueryParams().stream()
                .filter(param -> param.getName().equalsIgnoreCase(name))
                .findFirst().orElse(new NameValuePair() {
                    @Override
                    public String getName() {
                        return null;
                    }

                    @Override
                    public String getValue() {
                        return null;
                    }
                });
    }

    private static int indexOf(byte[] array, byte[] target, int start, int max) {
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
}
