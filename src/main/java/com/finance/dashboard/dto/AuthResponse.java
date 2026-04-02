package com.finance.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// This is what we send back after successful login or register
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;   // The JWT token the client must save
    private String name;
    private String role;
    private String message;
}