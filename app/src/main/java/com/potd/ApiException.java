package com.potd;

/**
 * Created by sanjay.rajput on 27/04/15.
 */

public class ApiException extends Exception {

    private final int status;
    private final String message;

    public ApiException(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
