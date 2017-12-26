package com.orshachar.knownissue.http.exceptions;

public class MethodHttpException extends Exception {
    private int statusCode;
    public MethodHttpException(String msg) {
        super(msg);
    }

    public MethodHttpException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;
    }

    public MethodHttpException(String msg, Exception cause) {
        super(msg, cause);
    }

    public MethodHttpException(String msg, int statusCode, Exception cause) {
        super(msg, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
