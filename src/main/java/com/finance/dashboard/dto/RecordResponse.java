package com.finance.dashboard.dto;

import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.FinancialRecord;
import com.finance.dashboard.model.RecordType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RecordResponse {

    private Long id;
    private BigDecimal amount;
    private RecordType type;
    private Category category;
    private LocalDate date;
    private String notes;
    private String createdBy; // Just the name, not the whole User object
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Static factory method — converts entity to DTO in one clean call
    // RecordResponse.from(record) is much cleaner than manually setting each field
    public static RecordResponse from(FinancialRecord record) {
        RecordResponse dto = new RecordResponse();
        dto.setId(record.getId());
        dto.setAmount(record.getAmount());
        dto.setType(record.getType());
        dto.setCategory(record.getCategory());
        dto.setDate(record.getDate());
        dto.setNotes(record.getNotes());
        dto.setCreatedBy(record.getCreatedBy().getName());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());
        return dto;
    }
}