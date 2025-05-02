package com.healthx.repository;

/**
 * 网络资源包装类，表示网络请求的状态和数据
 */
public class Resource<T> {
    
    public enum Status {
        SUCCESS,
        ERROR,
        LOADING
    }
    
    private final Status status;
    private final T data;
    private final String message;
    
    private Resource(Status status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }
    
    public static <T> Resource<T> success(T data) {
        return new Resource<>(Status.SUCCESS, data, null);
    }
    
    public static <T> Resource<T> error(String msg, T data) {
        return new Resource<>(Status.ERROR, data, msg);
    }
    
    public static <T> Resource<T> loading(T data) {
        return new Resource<>(Status.LOADING, data, null);
    }
    
    public Status getStatus() {
        return status;
    }
    
    public T getData() {
        return data;
    }
    
    public String getMessage() {
        return message;
    }
    
    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
    
    public boolean isLoading() {
        return status == Status.LOADING;
    }
    
    public boolean isError() {
        return status == Status.ERROR;
    }
} 