package com.szubp.psql_wal_recovery.dto;

import java.util.Map;

/**
 * Response za pregled WAL API-ja.
 */
public record OverviewResponse(
    String title,
    String description,
    Map<String, String> endpoints,
    Map<String, String> businessModels,
    String note
) {}

