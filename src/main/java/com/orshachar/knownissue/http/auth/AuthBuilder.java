package com.orshachar.knownissue.http.auth;

public class AuthBuilder {

    public static Auth basic(String username, String password) {
        return new BasicAuth(username, password);
    }
}
