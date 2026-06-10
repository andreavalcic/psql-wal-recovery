package com.szubp.psql_wal_recovery.dto;

/**
 * Statistika tabele iz pg_stat_user_tables.
 */
public record TableStatistics(
    Long insertCount,
    Long updateCount,
    Long deleteCount,
    Long hotUpdateCount,
    Long liveCount,
    Long deadCount
) {
    public Long getTotalModifications() {
        return insertCount + updateCount + deleteCount;
    }

    public Double getDeadTuplePercentage() {
        long total = liveCount + deadCount;
        if (total == 0) return 0.0;
        return (deadCount * 100.0) / total;
    }

    public boolean needsVacuum() {
        return deadCount > 1000 && getDeadTuplePercentage() > 5.0;
    }
}

