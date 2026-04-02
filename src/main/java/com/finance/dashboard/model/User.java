package com.finance.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity                    // Tells JPA this class maps to a database table
@Table(name = "users")     // The actual table name in MySQL
@Data                      // Lombok: auto-generates getters, setters, toString, equals
@NoArgsConstructor         // Lombok: generates empty constructor
@AllArgsConstructor        // Lombok: generates constructor with all fields
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increment ID
    private Long id;

    @Column(nullable = false, unique = true) // Email must exist and be unique
    private String email;

    @Column(nullable = false)
    private String password;  // This will be stored as a BCrypt hash, never plain text

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING) // Store role as text "ADMIN" not number
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean active = true; // Soft-disable users without deleting them

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist // This method runs automatically just before saving to DB
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}