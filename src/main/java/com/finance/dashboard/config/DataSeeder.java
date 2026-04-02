package com.finance.dashboard.config;

import com.finance.dashboard.model.*;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
// CommandLineRunner: runs once automatically after Spring Boot starts
// Perfect for seeding test data
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Only seed if DB is empty — prevents duplicate data on restart
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping.");
            return;
        }

        log.info("Seeding database with sample data...");

        // Create 3 users — one per role
        User admin = createUser("Admin User", "admin@finance.com", "admin123", Role.ADMIN);
        User analyst = createUser("Analyst User", "analyst@finance.com", "analyst123", Role.ANALYST);
        createUser("Viewer User", "viewer@finance.com", "viewer123", Role.VIEWER);

        // Seed 10 realistic financial records
        createRecord(new BigDecimal("85000.00"), RecordType.INCOME,
                Category.SALARY, LocalDate.of(2024, 1, 1),
                "January salary credit", admin);

        createRecord(new BigDecimal("12500.00"), RecordType.EXPENSE,
                Category.UTILITIES, LocalDate.of(2024, 1, 5),
                "Office rent and electricity for January", admin);

        createRecord(new BigDecimal("3200.00"), RecordType.EXPENSE,
                Category.FOOD, LocalDate.of(2024, 1, 10),
                "Team lunch and pantry restocking", admin);

        createRecord(new BigDecimal("25000.00"), RecordType.INCOME,
                Category.INVESTMENT, LocalDate.of(2024, 2, 1),
                "Returns from mutual fund SIP", admin);

        createRecord(new BigDecimal("8500.00"), RecordType.EXPENSE,
                Category.TRANSPORT, LocalDate.of(2024, 2, 8),
                "Cab reimbursements and fuel for February", admin);

        createRecord(new BigDecimal("85000.00"), RecordType.INCOME,
                Category.SALARY, LocalDate.of(2024, 2, 1),
                "February salary credit", admin);

        createRecord(new BigDecimal("15000.00"), RecordType.EXPENSE,
                Category.HEALTH, LocalDate.of(2024, 2, 14),
                "Team medical insurance premium", admin);

        createRecord(new BigDecimal("5600.00"), RecordType.EXPENSE,
                Category.ENTERTAINMENT, LocalDate.of(2024, 3, 5),
                "Team outing and annual day celebration", admin);

        createRecord(new BigDecimal("85000.00"), RecordType.INCOME,
                Category.SALARY, LocalDate.of(2024, 3, 1),
                "March salary credit", admin);

        createRecord(new BigDecimal("22000.00"), RecordType.EXPENSE,
                Category.EDUCATION, LocalDate.of(2024, 3, 20),
                "Online courses and certification fees for team", analyst);

        log.info("Seeding complete. Users: admin@finance.com / analyst@finance.com / viewer@finance.com (password same as role name + 123)");
    }

    private User createUser(String name, String email, String password, Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }

    private void createRecord(BigDecimal amount, RecordType type, Category category,
                              LocalDate date, String notes, User createdBy) {
        FinancialRecord record = new FinancialRecord();
        record.setAmount(amount);
        record.setType(type);
        record.setCategory(category);
        record.setDate(date);
        record.setNotes(notes);
        record.setCreatedBy(createdBy);
        record.setDeleted(false);
        recordRepository.save(record);
    }
}