package com.finance.dashboard.controller;

import com.finance.dashboard.dto.ApiResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Admin-only user management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "ADMIN only.")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", users));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Toggle user active/inactive status",
            description = "ADMIN only. Body: { \"active\": true } or { \"active\": false }"
    )
    public ResponseEntity<ApiResponse<Void>> toggleStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id));

        Boolean active = body.get("active");
        if (active == null) {
            throw new RuntimeException("Request body must contain 'active' boolean field");
        }

        user.setActive(active);
        userRepository.save(user);

        String status = active ? "activated" : "deactivated";
        return ResponseEntity.ok(ApiResponse.success("User " + status + " successfully", null));
    }
}