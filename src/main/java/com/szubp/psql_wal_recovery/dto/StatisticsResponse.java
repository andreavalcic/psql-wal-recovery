package com.szubp.psql_wal_recovery.dto;

/**
 * Response za statistiku business tabela.
 */
public record StatisticsResponse(
    TableStatistics dviReportSuiteStats,
    TableStatistics dviAttachmentStats,
    TableStatistics authUserStats,
    Long suiteTotalModifications,
    Long attachmentTotalModifications,
    Long suiteDeadTuples,
    Long attachmentDeadTuples,
    String explanation
) {}

