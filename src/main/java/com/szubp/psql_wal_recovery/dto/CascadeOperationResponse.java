package com.szubp.psql_wal_recovery.dto;

/**
 * Response za CASCADE operacije.
 */
public record CascadeOperationResponse(
    String suiteId,
    Integer attachmentsCount,
    Long totalWalBytes,
    Long averageWalPerEntity,
    Long cascadeDeleteWalBytes,
    String explanation
) {}

