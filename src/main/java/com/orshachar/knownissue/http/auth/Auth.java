package com.orshachar.knownissue.http.auth;

import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;

public abstract class Auth {

    abstract public void addAuth(HttpRequestBase request, CookieStore cookieStore);

    abstract public String asText();
}
