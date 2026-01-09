package ru.mipt.movies.auth.dto;

import ru.mipt.movies.auth.entity.Role;

public class UserInfoResponse {
    private String username;
    private Role role;

    public UserInfoResponse() {
    }

    public UserInfoResponse(String username, Role role) {
        this.username = username;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
