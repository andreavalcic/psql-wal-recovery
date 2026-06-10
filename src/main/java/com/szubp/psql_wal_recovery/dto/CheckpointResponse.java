package com.szubp.psql_wal_recovery.dto;

import java.util.Map;

/**
 * Response za checkpoint demonstraciju.
 */
public record CheckpointResponse(
    Long currentSuitesCount,
    Long currentUsersCount,
    Long currentAttachmentsCount,
    Long suiteTableSizeMb,
    Long attachmentTableSizeMb,
    String lsnBeforeCheckpoint,
    String lsnAfterCheckpoint,
    Long checkpointDurationMs,
    Map<String, Object> checkpointInfo,
    String explanation
) {}

