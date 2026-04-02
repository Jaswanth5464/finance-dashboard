package com.finance.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance; // totalIncome - totalExpense

    private Map<String, BigDecimal> categoryTotals;
    // e.g. { "FOOD": 5000.00, "TRANSPORT": 2000.00 }

    private Map<String, BigDecimal> monthlyTrends;
    // e.g. { "2024-01": 15000.00, "2024-02": 18000.00 }

    private List<RecordResponse> recentActivity;
    // Last 5 records for the dashboard feed

    private long totalRecordCount;
    private long incomeCount;
    private long expenseCount;
}