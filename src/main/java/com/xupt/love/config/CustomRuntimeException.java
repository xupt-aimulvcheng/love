package com.xupt.love.config;


public class CustomRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private int code;

    public CustomRuntimeException(int code, String message) {
        super(message);
        this.code = code;
    }
}
