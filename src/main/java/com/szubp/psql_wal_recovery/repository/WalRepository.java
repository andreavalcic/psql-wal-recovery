package com.szubp.psql_wal_recovery.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

/**
 * Repository za PostgreSQL WAL (Write-Ahead Logging) specifične upite.
 * Enkapsulira sve SQL upite vezane za WAL monitoring i analizu.
 */
@Repository
public class WalRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Vraća trenutnu LSN (Log Sequence Number) poziciju za WAL insert.
     */
    public String getCurrentWalInsertLsn() {
        return jdbcTemplate.queryForObject(
            "SELECT pg_current_wal_insert_lsn()::text",
            String.class
        );
    }

    /**
     * Vraća trenutnu LSN poziciju za WAL flush.
     */
    public String getCurrentWalFlushLsn() {
        return jdbcTemplate.queryForObject(
            "SELECT pg_current_wal_flush_lsn()::text",
            String.class
        );
    }

    /**
     * Izračunava razliku između dve LSN pozicije u bajtovima.
     */
    public Long calculateWalBytesGenerated(String lsnAfter, String lsnBefore) {
        return jdbcTemplate.queryForObject(
                "SELECT pg_wal_lsn_diff(?::pg_lsn, ?::pg_lsn)",
                Long.class,
                lsnAfter,
                lsnBefore
        );
    }

    /**
     * Vraća ukupnu veličinu tabele (sa indeksima).
     */
    public Long getTableSize(String tableName) {
        return jdbcTemplate.queryForObject(
            "SELECT pg_total_relation_size(?)",
            Long.class,
            tableName
        );
    }

    /**
     * Vraća statistiku modifikacija za datu tabelu.
     */
    public Map<String, Object> getTableStats(String tableName) {
        return jdbcTemplate.queryForMap(
            "SELECT n_tup_ins, n_tup_upd, n_tup_del, n_tup_hot_upd, " +
            "n_live_tup, n_dead_tup, last_vacuum, last_autovacuum " +
            "FROM pg_stat_user_tables WHERE relname = ?",
            tableName
        );
    }

    /**
     * Forsira PostgreSQL checkpoint operaciju.
     */
    public void executeCheckpoint() {
        jdbcTemplate.execute("CHECKPOINT");
    }

    /**
     * Vraća informacije o poslednjem checkpoint-u.
     */
    public Map<String, Object> getCheckpointInfo() {
        return jdbcTemplate.queryForMap(
            "SELECT checkpoint_lsn, redo_lsn, timeline_id, checkpoint_time " +
            "FROM pg_control_checkpoint()"
        );
    }

    /**
     * Vraća opštu WAL statistiku.
     */
    public Map<String, Object> getWalStats() {
        return jdbcTemplate.queryForMap("SELECT * FROM pg_stat_wal");
    }

    /**
     * Vraća statistiku o background writer procesu.
     */
    public Map<String, Object> getBgWriterStats() {
        return jdbcTemplate.queryForMap("SELECT * FROM pg_stat_bgwriter");
    }
}

