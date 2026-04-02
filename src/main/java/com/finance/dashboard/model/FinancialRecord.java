package com.finance.dashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "financial_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    // BigDecimal for money — never use float/double for money
    // Floating point math has rounding errors: 0.1 + 0.2 = 0.30000000000000004
    // BigDecimal is exact

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecordType type; // INCOME or EXPENSE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private LocalDate date; // Just the date, not time — a record belongs to a day

    @Column(length = 500)
    private String notes; // Optional description

    @Column(nullable = false)
    private boolean deleted = false;
    // Soft delete flag — we never physically delete records
    // This preserves history and allows undo

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;
    // Track who created each record — important for audit trail
    // LAZY means User data is only loaded from DB when you actually access it

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist // Runs automatically before INSERT
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate // Runs automatically before UPDATE
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}