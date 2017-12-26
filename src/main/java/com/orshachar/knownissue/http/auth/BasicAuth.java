package com.orshachar.knownissue.http.auth;

import com.google.common.io.BaseEncoding;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;

public class BasicAuth extends Auth {

    private String username;
    private String password;

    public BasicAuth(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void addAuth(HttpRequestBase request, CookieStore cookieStore) {
        String basicAuthUnencoded = String.format("%s:%s", username, password);
        String basicAuth = "Basic " + BaseEncoding.base64().encode(basicAuthUnencoded.getBytes());

        request.addHeader("Authorization", basicAuth);
    }

    @Override
    public String asText() {
        return "User: " + username + ". Pass: " + password;
    }
}
