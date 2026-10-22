package com.hd.nepm.config;

public class LoginUser {
    private final String role;
    private final String uid;

    public LoginUser(String role, String uid) {
        this.role = role;
        this.uid = uid;
    }

    public String getRole() {
        return role;
    }

    public String getUid() {
        return uid;
    }
}
