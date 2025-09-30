package com.example.bankcards.dto.user;

import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;

public record UserResponse(String username, String email, Role role) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(user.getUsername(), user.getEmail(), user.getRole());
    }
}
