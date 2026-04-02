package com.finance.dashboard.repository;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // Find by ID but only if not soft-deleted
    // We always check deleted=false so deleted records are invisible
    Optional<FinancialRecord> findByIdAndDeletedFalse(Long id);

    // Main listing query with ALL optional filters
    // Spring Data JPA reads the method name and builds the SQL — this is the power of JPA
    // deleted=false ensures soft-deleted records never appear
    Page<FinancialRecord> findByDeletedFalseAndTypeAndCategoryAndDateBetween(
            RecordType type,
            Category category,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    // These simpler versions handle when some filters aren't provided
    Page<FinancialRecord> findByDeletedFalse(Pageable pageable);
    Page<FinancialRecord> findByDeletedFalseAndType(RecordType type, Pageable pageable);
    Page<FinancialRecord> findByDeletedFalseAndCategory(Category category, Pageable pageable);
    Page<FinancialRecord> findByDeletedFalseAndDateBetween(
            LocalDate start, LocalDate end, Pageable pageable);

    // Search in notes field — for the search feature
    // %keyword% pattern matches anywhere in the string
    Page<FinancialRecord> findByDeletedFalseAndNotesContainingIgnoreCase(
            String keyword, Pageable pageable);

    // Dashboard queries — these use JPQL (Java Persistence Query Language)
    // JPQL writes like SQL but uses class/field names, not table/column names

    // SUM of all income amounts
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
            "WHERE r.deleted = false AND r.type = 'INCOME'")
    BigDecimal sumIncome();

    // SUM of all expense amounts
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM FinancialRecord r " +
            "WHERE r.deleted = false AND r.type = 'EXPENSE'")
    BigDecimal sumExpense();

    // Group by category and sum — returns list of [category, total] pairs
    // Object[] is used because we're returning two different fields together
    @Query("SELECT r.category, SUM(r.amount) FROM FinancialRecord r " +
            "WHERE r.deleted = false " +
            "GROUP BY r.category ORDER BY SUM(r.amount) DESC")
    List<Object[]> sumByCategory();

    // Monthly trends — FUNCTION('DATE_FORMAT',...) formats date as "2024-01"
    @Query("SELECT FUNCTION('DATE_FORMAT', r.date, '%Y-%m'), SUM(r.amount) " +
            "FROM FinancialRecord r " +
            "WHERE r.deleted = false AND r.type = :type " +
            "GROUP BY FUNCTION('DATE_FORMAT', r.date, '%Y-%m') " +
            "ORDER BY FUNCTION('DATE_FORMAT', r.date, '%Y-%m') ASC")
    List<Object[]> monthlyTrendsByType(@Param("type") RecordType type);

    // Recent 5 records for dashboard activity feed
    List<FinancialRecord> findTop5ByDeletedFalseOrderByCreatedAtDesc();

    // Count queries for stats
    long countByDeletedFalseAndType(RecordType type);
    long countByDeletedFalse();
}