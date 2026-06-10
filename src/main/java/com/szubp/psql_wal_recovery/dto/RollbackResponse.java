package com.szubp.psql_wal_recovery.dto;

/**
 * Response za rollback demonstraciju.
 */
public record RollbackResponse(
    Long suitesCountBefore,
    Long suitesCountAfter,
    String tempSuiteId,
    Boolean rollbackTriggered,
    String errorMessage,
    Boolean rollbackSuccessful,
    String explanation
) {}

