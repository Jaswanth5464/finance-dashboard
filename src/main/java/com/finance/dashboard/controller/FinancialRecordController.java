package com.finance.dashboard.controller;

import com.finance.dashboard.dto.*;
import com.finance.dashboard.model.Category;
import com.finance.dashboard.model.RecordType;
import com.finance.dashboard.service.FinancialRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
@Tag(name = "Financial Records", description = "CRUD operations for financial records")
@SecurityRequirement(name = "bearerAuth")
// @SecurityRequirement tells Swagger this controller needs a token
public class FinancialRecordController {

    private final FinancialRecordService recordService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create a financial record",
            description = "ADMIN only. Creates a new income or expense record."
    )
    public ResponseEntity<ApiResponse<RecordResponse>> create(
            @Valid @RequestBody RecordRequest request,
            Authentication authentication) {
        String email = authentication.getName();
        RecordResponse response = recordService.create(request, email);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Record created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(
            summary = "Get all records",
            description = "All roles. Supports filtering by type, category, date range, keyword. Paginated."
    )
    public ResponseEntity<ApiResponse<Page<RecordResponse>>> getAll(
            @Parameter(description = "Filter by type: INCOME or EXPENSE")
            @RequestParam(required = false) RecordType type,
            @Parameter(description = "Filter by category e.g. FOOD, SALARY")
            @RequestParam(required = false) Category category,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Search keyword in notes field")
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        if (!List.of("date", "amount", "category", "type", "createdAt").contains(sortBy)) {
            sortBy = "date";
        }
        Page<RecordResponse> records = recordService.getAll(
                type, category, startDate, endDate, keyword, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Records fetched successfully", records));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'VIEWER')")
    @Operation(summary = "Get record by ID", description = "All roles.")
    public ResponseEntity<ApiResponse<RecordResponse>> getById(@PathVariable Long id) {
        RecordResponse response = recordService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Record fetched successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a record", description = "ADMIN only.")
    public ResponseEntity<ApiResponse<RecordResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody RecordRequest request) {
        RecordResponse response = recordService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Record updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Soft delete a record",
            description = "ADMIN only. Marks as deleted — data is preserved, not physically removed."
    )
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        recordService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.success("Record deleted successfully", null));
    }
}