package com.szubp.psql_wal_recovery.dto;

/**
 * Response za REDO demonstraciju.
 */
public record RedoOperationResponse(
    String testUserId,
    String testUserEmail,
    String createdSuiteId,
    String createdSuiteName,
    String lsnBefore,
    String lsnAfter,
    Long walBytesGenerated,
    Long totalSuitesInDb,
    String explanation
) {}

