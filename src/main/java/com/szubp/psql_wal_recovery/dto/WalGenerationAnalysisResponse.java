package com.szubp.psql_wal_recovery.dto;

/**
 * Response za WAL generation analizu - INSERT vs UPDATE vs DELETE.
 */
public record WalGenerationAnalysisResponse(
    Long walBytesInsertSuite,
    Long walBytesUpdateSuiteWithLargeField,
    Long walBytesInsertAttachment1kb,
    Long walBytesDeleteAttachment,
    String updateVsInsertRatio,
    String explanation
) {}

