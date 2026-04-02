package com.finance.dashboard.model;

// This is an enum — a fixed set of values
// We use it to represent the 3 roles in the system
// Storing as STRING means MySQL saves "ADMIN" not "0", which is readable
public enum Role {
    VIEWER,   // Can only read data
    ANALYST,  // Can read + view dashboard summaries
    ADMIN     // Full access: create, update, delete, manage users
}