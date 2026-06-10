package com.szubp.psql_wal_recovery.controller;

import com.szubp.psql_wal_recovery.dto.*;
import com.szubp.psql_wal_recovery.service.WalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * REST Controller za WAL (Write-Ahead Logging) operacije sa stvarnim business modelima.
 *
 * Demonstrira kako PostgreSQL WAL funkcioniše kroz:
 * - XbrlReportSuite entitete
 * - AuthUser relacione veze
 * - XbrlAttachment BLOB podatke
 * - CASCADE operacije
 *
 * Endpoint: /api/wal/*
 */
@RestController
@RequestMapping("/api/wal")
public class WalController {

    @Autowired
    private WalService walService;

    /**
     * Pregled svih dostupnih WAL operacija sa business modelima.
     *
     * GET /api/wal/overview
     */
    @GetMapping("/overview")
    public ResponseEntity<OverviewResponse> getOverview() {
        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("GET /api/wal/overview", "Pregled API-ja");
        endpoints.put("GET /api/wal/redo", "REDO operacija sa XbrlReportSuite");
        endpoints.put("GET /api/wal/update-analysis", "UPDATE operacije i MVCC");
        endpoints.put("GET /api/wal/rollback", "Rollback scenario sa MVCC");
        endpoints.put("GET /api/wal/wal-generation", "Analiza WAL generisanja");
        endpoints.put("GET /api/wal/cascade", "CASCADE operacije i WAL");
        endpoints.put("GET /api/wal/checkpoint", "Checkpoint mehanizam");
        endpoints.put("GET /api/wal/statistics", "Business tabele statistika");

        Map<String, String> models = new LinkedHashMap<>();
        models.put("XbrlReportSuite", "dvi_report_suite - glavni dokument entitet");
        models.put("AuthUser", "auth_user - korisnici sistema");
        models.put("XbrlAttachment", "dvi_attachment - dokumenti sa BLOB podacima");
        models.put("XbrlSuiteDelivery", "xbr_report_suite_delivery - delivery tracking");

        OverviewResponse response = new OverviewResponse(
            "PostgreSQL WAL (Write-Ahead Logging) API",
            "WAL mehanizmi kroz stvarne business entitete",
            endpoints,
            models,
            "Svi endpoints generišu stvarne WAL zapise u PostgreSQL-u"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Demonstrira REDO operaciju - replay committed transakcija nakon crash-a.
     * Kreira novi XbrlReportSuite entitet i mjeri WAL generation.
     *
     * GET /api/wal/redo
     */
    @GetMapping("/redo")
    public ResponseEntity<RedoOperationResponse> demonstrateRedo() {
        return ResponseEntity.ok(walService.demonstrateRedoWithRealData());
    }

    /**
     * Demonstrira UPDATE operaciju koja generiše najviše WAL-a zbog MVCC.
     * PostgreSQL čuva staru i novu verziju tuple-a.
     *
     * GET /api/wal/update-analysis
     */
    @GetMapping("/update-analysis")
    public ResponseEntity<UpdateAnalysisResponse> analyzeUpdate() {
        return ResponseEntity.ok(walService.demonstrateUpdateWalGeneration());
    }

    /**
     * Demonstrira rollback mehanizam sa MVCC.
     * Necommitted izmjene ostaju nevidljive drugim transakcijama.
     *
     * GET /api/wal/rollback
     */
    @GetMapping("/rollback")
    public ResponseEntity<RollbackResponse> demonstrateRollback() {
        return ResponseEntity.ok(walService.demonstrateRollbackWithMVCC());
    }

    /**
     * Analizira koliko WAL-a generišu različite operacije:
     * - INSERT XbrlReportSuite
     * - UPDATE sa velikim XBRL fieldom
     * - INSERT XbrlAttachment (BLOB)
     * - DELETE operacije
     *
     * GET /api/wal/wal-generation
     */
    @GetMapping("/wal-generation")
    public ResponseEntity<WalGenerationAnalysisResponse> analyzeWalGeneration() {
        return ResponseEntity.ok(walService.analyzeRealWorldWalGeneration());
    }

    /**
     * Demonstrira kako CASCADE operacije utiču na WAL generation.
     * Kreira XbrlReportSuite sa multiple XbrlAttachment entitetima.
     *
     * GET /api/wal/cascade
     */
    @GetMapping("/cascade")
    public ResponseEntity<CascadeOperationResponse> demonstrateCascade() {
        return ResponseEntity.ok(walService.demonstrateCascadeWalGeneration());
    }

    /**
     * Forsira CHECKPOINT operaciju sa stvarnim business podacima.
     * Mjeri trajanje, dirty pages flush, i recovery point.
     *
     * GET /api/wal/checkpoint
     */
    @GetMapping("/checkpoint")
    public ResponseEntity<CheckpointResponse> demonstrateCheckpoint() {
        return ResponseEntity.ok(walService.demonstrateCheckpointWithRealData());
    }

    /**
     * Prikazuje WAL statistiku za business tabele:
     * - dvi_report_suite
     * - dvi_attachment
     * - auth_user
     *
     * Uključuje: tuple counts, dead tuples, HOT updates, VACUUM info.
     *
     * GET /api/wal/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> getStatistics() {
        return ResponseEntity.ok(walService.getBusinessTablesWalStatistics());
    }
}
