package com.healthx.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.room.ColumnInfo;
import java.io.Serializable;

/**
 * 用户实体类
 */
@Entity(tableName = "users")
public class User implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private Long id;
    
    @ColumnInfo(name = "username")
    private String username;
    
    @ColumnInfo(name = "email")
    private String email;
    
    @ColumnInfo(name = "nickname")
    private String nickname;
    
    @Ignore // token不需要持久化到数据库
    private String token;
    
    @ColumnInfo(name = "gender")
    private String gender;
    
    @ColumnInfo(name = "age")
    private Integer age;
    
    @ColumnInfo(name = "height")
    private Double height;
    
    @ColumnInfo(name = "weight")
    private Double weight;

    // 构造函数
    public User() {
    }

    @Ignore // 包含token的构造函数不应被Room使用
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