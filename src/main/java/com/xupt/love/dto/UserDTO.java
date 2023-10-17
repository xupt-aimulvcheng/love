package com.xupt.love.dto;

public class UserDTO {

    private String username;
    private String password;
    private String email;
    private String emailCode; // 邮箱验证码

    // 无参构造方法
    public UserDTO() {
    }

    // 有参构造方法
    public UserDTO(String username, String password, String email, String emailCode) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.emailCode = emailCode;
    }

    // Getter 和 Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmailCode() {
        return emailCode;
    }

    public void setEmailCode(String emailCode) {
        this.emailCode = emailCode;
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", emailCode='" + emailCode + '\'' +
                '}';
    }
}
