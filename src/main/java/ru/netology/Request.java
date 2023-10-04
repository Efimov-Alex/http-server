package ru.netology;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;

public class Request {
    private String method;
    private String headers;

    String[] listHeaders;

    public Request(String method, String headers) {
        this.method = method;
        this.headers = headers;
        listHeaders = headers.split("\\?");


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
}
