package com.healthx.model;

import java.io.Serializable;

/**
 * 用户实体类
 */
public class User implements Serializable {

    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String token;
    private String gender;
    private Integer age;
    private Double height;
    private Double weight;

    // 构造函数
    public User() {
    }

    public User(Long id, String username, String email, String nickname, String token) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.token = token;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }
} 