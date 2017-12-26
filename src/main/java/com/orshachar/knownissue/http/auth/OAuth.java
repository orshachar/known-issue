package com.orshachar.knownissue.http.auth;

import java.io.IOException;
import org.apache.http.HttpResponse;

public abstract class OAuth extends Auth {

    abstract public boolean doRefresh(HttpResponse response) throws IOException;
}
