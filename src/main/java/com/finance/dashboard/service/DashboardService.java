package com.finance.dashboard.service;

import com.finance.dashboard.dto.DashboardSummaryResponse;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.model.RecordType;
import com.finance.dashboard.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        // Get all the numbers from the DB
        BigDecimal totalIncome = recordRepository.sumIncome();
        BigDecimal totalExpense = recordRepository.sumExpense();
        BigDecimal netBalance = totalIncome.subtract(totalExpense);
        // .subtract() is BigDecimal's safe subtraction — no floating point errors

        // Build category totals map from the Object[] query results
        Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
        // LinkedHashMap preserves insertion order (highest total first because of ORDER BY)
        for (Object[] row : recordRepository.sumByCategory()) {
            String category = row[0].toString(); // First column: category name
            BigDecimal total = (BigDecimal) row[1]; // Second column: sum
            categoryTotals.put(category, total);
        }

        // Build monthly trends for INCOME
        Map<String, BigDecimal> monthlyTrends = new LinkedHashMap<>();
        for (Object[] row : recordRepository.monthlyTrendsByType(RecordType.INCOME)) {
            monthlyTrends.put(row[0].toString(), (BigDecimal) row[1]);
        }

        // Last 5 records for the activity feed
        List<RecordResponse> recentActivity = recordRepository
                .findTop5ByDeletedFalseOrderByCreatedAtDesc()
                .stream()
                .map(RecordResponse::from)
                .collect(Collectors.toList());
        // .stream().map().collect() is Java's functional way to transform a list

        long totalCount = recordRepository.countByDeletedFalse();
        long incomeCount = recordRepository.countByDeletedFalseAndType(RecordType.INCOME);
        long expenseCount = recordRepository.countByDeletedFalseAndType(RecordType.EXPENSE);

        return new DashboardSummaryResponse(
                totalIncome,
                totalExpense,
                netBalance,
                categoryTotals,
                monthlyTrends,
                recentActivity,
                totalCount,
                incomeCount,
                expenseCount
        );
    }
}