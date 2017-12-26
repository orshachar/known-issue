package com.orshachar.knownissue.http.exceptions;

public class MethodBuildException extends RuntimeException {

    public MethodBuildException(String msg) {
        super(msg);
    }

    public MethodBuildException(String msg, Exception cause) {
        super(msg, cause);
    }
}
