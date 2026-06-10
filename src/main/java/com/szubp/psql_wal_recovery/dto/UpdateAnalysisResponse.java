package com.szubp.psql_wal_recovery.dto;

/**
 * Response za UPDATE/MVCC analizu.
 */
public record UpdateAnalysisResponse(
    String suiteId,
    String originalStatus,
    String newStatus,
    String lsnBeforeUpdate,
    String lsnAfterUpdate,
    Long walBytesForUpdate,
    String explanation
) {}

