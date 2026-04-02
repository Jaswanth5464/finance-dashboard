package com.finance.dashboard.service;

import com.finance.dashboard.dto.RecordRequest;
import com.finance.dashboard.dto.RecordResponse;
import com.finance.dashboard.exception.ResourceNotFoundException;
import com.finance.dashboard.model.*;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class) activates Mockito — no Spring context needed
// Tests run fast because nothing is loaded from DB
@ExtendWith(MockitoExtension.class)
class FinancialRecordServiceTest {

    @Mock // Creates a fake repository — no real DB calls
    private FinancialRecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks // Creates FinancialRecordService and injects the mocks above
    private FinancialRecordService recordService;

    private User testUser;
    private RecordRequest validRequest;

    @BeforeEach // Runs before each test method
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("admin@finance.com");
        testUser.setRole(Role.ADMIN);
        testUser.setActive(true);

        validRequest = new RecordRequest();
        validRequest.setAmount(new BigDecimal("50000.00"));
        validRequest.setType(RecordType.INCOME);
        validRequest.setCategory(Category.SALARY);
        validRequest.setDate(LocalDate.of(2024, 1, 15));
        validRequest.setNotes("Test salary");
    }

    @Test
    void create_shouldReturnRecordResponse_whenValidRequest() {
        // ARRANGE — set up what mocks should return
        when(userRepository.findByEmail("admin@finance.com"))
                .thenReturn(Optional.of(testUser));

        FinancialRecord savedRecord = new FinancialRecord();
        savedRecord.setId(1L);
        savedRecord.setAmount(validRequest.getAmount());
        savedRecord.setType(validRequest.getType());
        savedRecord.setCategory(validRequest.getCategory());
        savedRecord.setDate(validRequest.getDate());
        savedRecord.setNotes(validRequest.getNotes());
        savedRecord.setCreatedBy(testUser);
        savedRecord.setDeleted(false);

        when(recordRepository.save(any(FinancialRecord.class))).thenReturn(savedRecord);

        // ACT — call the method being tested
        RecordResponse response = recordService.create(validRequest, "admin@finance.com");

        // ASSERT — verify the result
        assertThat(response).isNotNull();
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        assertThat(response.getType()).isEqualTo(RecordType.INCOME);
        assertThat(response.getCategory()).isEqualTo(Category.SALARY);

        // Verify repository was called exactly once
        verify(recordRepository, times(1)).save(any(FinancialRecord.class));
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenRecordNotFound() {
        // ARRANGE — mock returns empty (record doesn't exist)
        when(recordRepository.findByIdAndDeletedFalse(999L))
                .thenReturn(Optional.empty());

        // ACT + ASSERT — verify that the right exception is thrown
        assertThatThrownBy(() -> recordService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void softDelete_shouldMarkRecordAsDeleted() {
        // ARRANGE
        FinancialRecord record = new FinancialRecord();
        record.setId(1L);
        record.setDeleted(false);
        record.setCreatedBy(testUser);
        record.setAmount(new BigDecimal("1000"));
        record.setType(RecordType.EXPENSE);
        record.setCategory(Category.FOOD);
        record.setDate(LocalDate.now());

        when(recordRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(record));
        when(recordRepository.save(any())).thenReturn(record);

        // ACT
        recordService.softDelete(1L);

        // ASSERT — verify the record was marked deleted and saved
        assertThat(record.isDeleted()).isTrue();
        verify(recordRepository, times(1)).save(record);
    }

    @Test
    void create_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByEmail("ghost@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.create(validRequest, "ghost@test.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}