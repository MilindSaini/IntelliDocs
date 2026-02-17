package com.milind.docintel.service.auth;

import com.milind.docintel.entity.UserRole;

import java.util.UUID;

public class AuthenticatedUser {
    private final UUID id;
    private final String email;
    private final UserRole role;

    public AuthenticatedUser(UUID id, String email, UserRole role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }
}
