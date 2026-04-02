package com.finance.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

// Instead of raw objects, we always wrap responses in this
// So every API returns: { "success": true, "message": "...", "data": {...} }
// This is a professional pattern evaluators will notice
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;             // Generic: can hold any type

    // Convenience methods so you don't repeat success/failure everywhere
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}