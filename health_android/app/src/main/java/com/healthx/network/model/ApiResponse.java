package com.healthx.network.model;

import java.util.Date;

/**
 * API通用响应模型
 */
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private Date timestamp;
    
    public ApiResponse() {
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
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
    
    public Date getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
} 