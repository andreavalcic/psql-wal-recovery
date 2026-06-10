package com.szubp.psql_wal_recovery.service;

import com.szubp.psql_wal_recovery.db.model.*;
import com.szubp.psql_wal_recovery.dto.*;
import com.szubp.psql_wal_recovery.repository.AuthUserRepository;
import com.szubp.psql_wal_recovery.repository.WalRepository;
import com.szubp.psql_wal_recovery.repository.XbrlAttachmentRepository;
import com.szubp.psql_wal_recovery.repository.XbrlReportSuiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;

/**
 * Service za WAL (Write-Ahead Logging) operacije - REFAKTORISANA VERZIJA.
 *
 * Koristi stvarne business entitete za demonstraciju WAL mehanizama.
 * Vraća type-safe DTOs umesto Map<String, Object>.
 */
@Service
public class WalService {

    @Autowired
    private XbrlReportSuiteRepository suiteRepository;

    @Autowired
    private AuthUserRepository userRepository;

    @Autowired
    private XbrlAttachmentRepository attachmentRepository;

    @Autowired
    private WalRepository walRepository;

    @Autowired
    private WalOperationTracker tracker;

    @Autowired
    private WalTestDataFactory testDataFactory;

    /**
     * REDO demonstracija - INSERT operacija.
     */
    @Transactional
    public RedoOperationResponse demonstrateRedoWithRealData() {
        AuthUser testUser = testDataFactory.getOrCreateTestUser();

        // Track WAL generation
        WalOperationTracker.TrackedResult<XbrlReportSuite> tracked = tracker.trackWithResult(() -> {
            XbrlReportSuite suite = testDataFactory.createNewTestSuite(testUser, "WAL_DEMO_SUITE");
            suite.setXbrl("<xbrl><demo>WAL REDO Demonstracija</demo></xbrl>");
            return suiteRepository.saveAndFlush(suite);
        });

        XbrlReportSuite savedSuite = tracked.result();
        WalOperationResult walResult = tracked.walTracking();

        String explanation = String.format(
            "REDO: Kreiran je novi XbrlReportSuite. INSERT je zapisao %s u WAL. " +
            "Posle pada, PostgreSQL replay-a ovaj zapis i suite '%s' bi bio oporavljen.",
            walResult.getWalSizePretty(),
            savedSuite.getId()
        );

        return new RedoOperationResponse(
            testUser.getId(),
            testUser.getEmail(),
            savedSuite.getId(),
            savedSuite.getName(),
            walResult.lsnBefore(),
            walResult.lsnAfter(),
            walResult.walBytesGenerated(),
            suiteRepository.count(),
            explanation
        );
    }

    /**
     * UPDATE demonstracija - MVCC overhead.
     */
    @Transactional
    public UpdateAnalysisResponse demonstrateUpdateWalGeneration() {
        AuthUser testUser = testDataFactory.getOrCreateTestUser();
        XbrlReportSuite suite = testDataFactory.getOrCreateTestSuite(testUser);

        String originalStatus = suite.getStatus().name();

        // Track UPDATE
        WalOperationResult walResult = tracker.track(() -> {
            suite.setStatus(XbrlDocumentStatus.COMPLETED);
            suite.setModifiedAt(new Date());
            suite.setCompletedAt(new Date());
            suite.setCompletedBy(testUser);
            suite.setXbrl(suite.getXbrl() + " <updated>Modified for WAL demo</updated>");
            suiteRepository.saveAndFlush(suite);
        });

        String explanation = String.format(
            "UPDATE: MVCC čuva STARU + NOVU verziju = više WAL-a. Generisano: %s",
            walResult.getWalSizePretty()
        );

        return new UpdateAnalysisResponse(
            suite.getId(),
            originalStatus,
            suite.getStatus().name(),
            walResult.lsnBefore(),
            walResult.lsnAfter(),
            walResult.walBytesGenerated(),
            explanation
        );
    }

    /**
     * Rollback demonstracija - MVCC.
     */
    @Transactional
    public RollbackResponse demonstrateRollbackWithMVCC() {
        AuthUser testUser = testDataFactory.getOrCreateTestUser();
        long countBefore = suiteRepository.count();

        String tempSuiteId = null;
        boolean rollbackTriggered = false;
        String errorMessage = null;

        try {
            XbrlReportSuite tempSuite = testDataFactory.createNewTestSuite(testUser, "ROLLBACK_TEST");
            tempSuite.setXbrl("<xbrl><rollback>This will be rolled back</rollback></xbrl>");
            XbrlReportSuite saved = suiteRepository.save(tempSuite);
            tempSuiteId = saved.getId();

            throw new RuntimeException("Simulated error for MVCC/Rollback demonstration");

        } catch (RuntimeException e) {
            rollbackTriggered = true;
            errorMessage = e.getMessage();
        }

        long countAfter = suiteRepository.count();

        return new RollbackResponse(
            countBefore,
            countAfter,
            tempSuiteId,
            rollbackTriggered,
            errorMessage,
            countBefore == countAfter,
            "MVCC ROLLBACK: Tuple postoji ali je NEVIDLJIV (marked as aborted). VACUUM će ga očistiti."
        );
    }

    /**
     * WAL generation analiza - INSERT vs UPDATE vs DELETE.
     */
    @Transactional
    public WalGenerationAnalysisResponse analyzeRealWorldWalGeneration() {
        AuthUser testUser = testDataFactory.getOrCreateTestUser();

        // TEST 1: INSERT
        WalOperationTracker.TrackedResult<XbrlReportSuite> insertResult = tracker.trackWithResult(() -> {
            XbrlReportSuite suite = testDataFactory.createNewTestSuite(testUser, "WAL_ANALYSIS_INSERT");
            return suiteRepository.saveAndFlush(suite);
        });

        // TEST 2: UPDATE sa velikim field-om
        XbrlReportSuite suite = insertResult.result();
        WalOperationResult updateResult = tracker.track(() -> {
            suite.setXbrl(suite.getXbrl() + testDataFactory.generateLargeXbrl(500));
            suite.setStatus(XbrlDocumentStatus.COMPLETED);
            suite.setModifiedAt(new Date());
            suiteRepository.saveAndFlush(suite);
        });

        // TEST 3: INSERT Attachment (BLOB)
        WalOperationTracker.TrackedResult<XbrlAttachment> attachmentResult = tracker.trackWithResult(() -> {
            XbrlAttachment att = testDataFactory.createTestAttachment(
                suite, testUser, "wal_demo_attachment.pdf", 1024, XbrlAttachmentType.ACCOUNT_PDF
            );
            return attachmentRepository.saveAndFlush(att);
        });

        // TEST 4: DELETE
        WalOperationResult deleteResult = tracker.track(() -> {
            attachmentRepository.delete(attachmentResult.result());
            attachmentRepository.flush();
        });

        Long insertWal = insertResult.walTracking().walBytesGenerated();
        Long updateWal = updateResult.walBytesGenerated();
        String ratio = insertWal > 0
            ? String.format("%.2fx", (double) updateWal / insertWal)
            : "N/A";

        // Cleanup
        suiteRepository.delete(suite);

        return new WalGenerationAnalysisResponse(
            insertWal,
            updateWal,
            attachmentResult.walTracking().walBytesGenerated(),
            deleteResult.walBytesGenerated(),
            ratio,
            String.format(
                "WAL ANALIZA: INSERT=%s, UPDATE=%s (MVCC overhead), DELETE=%s",
                insertResult.walTracking().getWalSizePretty(),
                updateResult.getWalSizePretty(),
                deleteResult.getWalSizePretty()
            )
        );
    }

    /**
     * CASCADE demonstracija.
     */
    @Transactional
    public CascadeOperationResponse demonstrateCascadeWalGeneration() {
        AuthUser testUser = testDataFactory.getOrCreateTestUser();

        // CASCADE INSERT
        WalOperationTracker.TrackedResult<XbrlReportSuite> cascadeResult = tracker.trackWithResult(() -> {
            XbrlReportSuite suite = testDataFactory.createNewTestSuite(testUser, "CASCADE_WAL_DEMO");
            suite.setXbrl("<xbrl><cascade>CASCADE WAL demo</cascade></xbrl>");

            XbrlAttachmentType[] attachmentTypes = {
                    XbrlAttachmentType.ACCOUNT_PDF,
                    XbrlAttachmentType.ACC_VERKLARING,
                    XbrlAttachmentType.AEDES_PROPS_TBL
            };

            for (int i = 1; i <= 3; i++) {
                XbrlAttachment att = testDataFactory.createTestAttachment(
                    suite, testUser, "cascade_attachment_" + i + ".pdf", 512, attachmentTypes[i-1]
                );
                suite.getAttachments().add(att);
            }
            return suiteRepository.saveAndFlush(suite);
        });

        XbrlReportSuite savedSuite = cascadeResult.result();
        WalOperationResult walResult = cascadeResult.walTracking();

        // CASCADE DELETE
        WalOperationResult deleteResult = tracker.track(() -> {
            suiteRepository.delete(savedSuite);
            suiteRepository.flush();
        });

        return new CascadeOperationResponse(
            savedSuite.getId(),
            savedSuite.getAttachments().size(),
            walResult.walBytesGenerated(),
            walResult.walBytesGenerated() / 4,
            deleteResult.walBytesGenerated(),
            String.format(
                "CASCADE: 1 save() = WAL za suite + 3 attachmenta = %s. DELETE = %s.",
                walResult.getWalSizePretty(),
                deleteResult.getWalSizePretty()
            )
        );
    }

    /**
     * Checkpoint demonstracija.
     */
    public CheckpointResponse demonstrateCheckpointWithRealData() {
        Long suiteSize = walRepository.getTableSize("dvi_report_suite");
        Long attachmentSize = walRepository.getTableSize("dvi_attachment");

        String lsnBefore = walRepository.getCurrentWalInsertLsn();

        long startTime = System.currentTimeMillis();
        walRepository.executeCheckpoint();
        long duration = System.currentTimeMillis() - startTime;

        String lsnAfter = walRepository.getCurrentWalInsertLsn();
        Map<String, Object> checkpointInfo = walRepository.getCheckpointInfo();

        return new CheckpointResponse(
            suiteRepository.count(),
            userRepository.count(),
            attachmentRepository.count(),
            suiteSize / (1024 * 1024),
            attachmentSize / (1024 * 1024),
            lsnBefore,
            lsnAfter,
            duration,
            checkpointInfo,
            String.format(
                "CHECKPOINT: Flush dirty pages na disk. Trajao: %d ms. Recovery počinje od redo_lsn.",
                duration
            )
        );
    }

    /**
     * Statistika business tabela.
     */
    public StatisticsResponse getBusinessTablesWalStatistics() {
        TableStatistics suiteStats = mapToTableStats(walRepository.getTableStats("dvi_report_suite"));
        TableStatistics attachmentStats = mapToTableStats(walRepository.getTableStats("dvi_attachment"));
        TableStatistics userStats = mapToTableStats(walRepository.getTableStats("auth_user"));

        return new StatisticsResponse(
            suiteStats,
            attachmentStats,
            userStats,
            suiteStats.getTotalModifications(),
            attachmentStats.getTotalModifications(),
            suiteStats.deadCount(),
            attachmentStats.deadCount(),
            String.format(
                "STATISTIKA: dvi_report_suite=%d modifikacija, %d dead tuples (%.2f%%). VACUUM %s potreban.",
                suiteStats.getTotalModifications(),
                suiteStats.deadCount(),
                suiteStats.getDeadTuplePercentage(),
                suiteStats.needsVacuum() ? "JESTE" : "NIJE"
            )
        );
    }

    private TableStatistics mapToTableStats(Map<String, Object> stats) {
        return new TableStatistics(
            ((Number) stats.get("n_tup_ins")).longValue(),
            ((Number) stats.get("n_tup_upd")).longValue(),
            ((Number) stats.get("n_tup_del")).longValue(),
            ((Number) stats.get("n_tup_hot_upd")).longValue(),
            ((Number) stats.get("n_live_tup")).longValue(),
            ((Number) stats.get("n_dead_tup")).longValue()
        );
    }
}

