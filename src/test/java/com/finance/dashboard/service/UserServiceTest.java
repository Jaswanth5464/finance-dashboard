package com.finance.dashboard.service;

import com.finance.dashboard.dto.AuthResponse;
import com.finance.dashboard.dto.LoginRequest;
import com.finance.dashboard.dto.RegisterRequest;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks private UserService userService;

    @Test
    void register_shouldSucceed_whenEmailNotTaken() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@test.com");
        request.setPassword("pass123");
        request.setRole(Role.VIEWER);

        when(userRepository.existsByEmail("test@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(jwtUtil.generateToken("test@test.com")).thenReturn("mock-token");

        AuthResponse response = userService.register(request);

        assertThat(response.getToken()).isEqualTo("mock-token");
        assertThat(response.getRole()).isEqualTo("VIEWER");
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("duplicate@test.com");

        when(userRepository.existsByEmail("duplicate@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void login_shouldThrowException_whenPasswordWrong() {
        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("wrongpassword");

        User user = new User();
        user.setEmail("user@test.com");
        user.setPassword("correcthashedpassword");
        user.setActive(true);
        user.setRole(Role.ADMIN);

        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "correcthashedpassword")).thenReturn(false);

        // Same error message for wrong email OR wrong password — security best practice
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_shouldThrowException_whenUserInactive() {
        LoginRequest request = new LoginRequest();
        request.setEmail("inactive@test.com");
        request.setPassword("pass123");

        User user = new User();
        user.setEmail("inactive@test.com");
        user.setPassword("hashed");
        user.setActive(false); // Disabled account
        user.setRole(Role.VIEWER);

        when(userRepository.findByEmail("inactive@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("disabled");
    }
}