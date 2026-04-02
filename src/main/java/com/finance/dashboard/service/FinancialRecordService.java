package com.finance.dashboard.service;

import com.finance.dashboard.dto.RecordRequest;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.*;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {

    private final FinancialRecordRepository recordRepository;
    private final UserRepository userRepository;

    // Creates a new financial record
    // @Transactional means: if anything fails, the whole thing rolls back
    // So you never get a half-saved record in the DB
    @Transactional
    public RecordResponse create(RecordRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);

        FinancialRecord record = new FinancialRecord();
        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());
        record.setCreatedBy(user);
        record.setDeleted(false);

        return RecordResponse.from(recordRepository.save(record));
    }

    // Returns paginated, filtered list of records
    // All params are optional — null means "don't filter by this"
    public Page<RecordResponse> getAll(
            RecordType type,
            Category category,
            LocalDate startDate,
            LocalDate endDate,
            String keyword,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        // Build sort direction from string parameter
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<FinancialRecord> records;

        // Search takes priority over other filters
        if (keyword != null && !keyword.isBlank()) {
            records = recordRepository
                    .findByDeletedFalseAndNotesContainingIgnoreCase(keyword, pageable);
        }
        // All filters provided
        else if (type != null && category != null && startDate != null && endDate != null) {
            records = recordRepository
                    .findByDeletedFalseAndTypeAndCategoryAndDateBetween(
                            type, category, startDate, endDate, pageable);
        }
        // Only type filter
        else if (type != null && category == null && startDate == null) {
            records = recordRepository.findByDeletedFalseAndType(type, pageable);
        }
        // Only category filter
        else if (category != null && type == null && startDate == null) {
            records = recordRepository.findByDeletedFalseAndCategory(category, pageable);
        }
        // Only date range filter
        else if (startDate != null && endDate != null) {
            records = recordRepository
                    .findByDeletedFalseAndDateBetween(startDate, endDate, pageable);
        }
        // No filters — return all
        else {
            records = recordRepository.findByDeletedFalse(pageable);
        }

        // Convert each entity to DTO using the .from() factory method
        return records.map(RecordResponse::from);
    }

    // Get single record by ID
    public RecordResponse getById(Long id) {
        FinancialRecord record = findActiveRecordById(id);
        return RecordResponse.from(record);
    }

    // Update existing record — only updates fields that are provided
    @Transactional
    public RecordResponse update(Long id, RecordRequest request) {
        FinancialRecord record = findActiveRecordById(id);

        record.setAmount(request.getAmount());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setDate(request.getDate());
        record.setNotes(request.getNotes());
        // updatedAt is set automatically by @PreUpdate

        return RecordResponse.from(recordRepository.save(record));
    }

    // Soft delete — sets deleted=true instead of removing from DB
    // This preserves history and dashboard calculations for past periods
    @Transactional
    public void softDelete(Long id) {
        FinancialRecord record = findActiveRecordById(id);
        record.setDeleted(true);
        recordRepository.save(record);
        // Record still exists in DB with deleted=true
        // All queries filter deleted=false so it disappears from all responses
    }

    // Helper to find active (non-deleted) record or throw clean 404
    private FinancialRecord findActiveRecordById(Long id) {
        return recordRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Record not found with id: " + id));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email));
    }
}