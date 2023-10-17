package com.xupt.love.config.enums;

public class Result<T> {
    
    private int statusCode;
    private String message;
    private T data;

    public Result(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public Result(int statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
    }

    // getters and setters

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
    public static Result success(String message) {
        return new Result(200, message);
    }

    public static Result success(String message, Object data) {
        return new Result(200, message, data);
    }

    public static Result fail(String message) {
        return new Result(400, message);
    }
}
