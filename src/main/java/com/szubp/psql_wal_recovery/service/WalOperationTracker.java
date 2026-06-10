package com.szubp.psql_wal_recovery.service;

import com.szubp.psql_wal_recovery.dto.WalOperationResult;
import com.szubp.psql_wal_recovery.repository.WalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper klasa za tracking WAL generisanja tokom operacija.
 * Automatski meri LSN prije i poslije operacije.
 */
@Component
public class WalOperationTracker {

    @Autowired
    private WalRepository walRepository;

    /**
     * Izvršava operaciju i automatski tracka koliko WAL-a je generisano.
     *
     * @param operation Operacija koja generiše WAL
     * @return Rezultat sa LSN pozicijama i generisanim bajtovima
     */
    public WalOperationResult track(Runnable operation) {
        String lsnBefore = walRepository.getCurrentWalInsertLsn();

        operation.run();

        String lsnAfter = walRepository.getCurrentWalInsertLsn();
        Long bytesGenerated = walRepository.calculateWalBytesGenerated(lsnAfter, lsnBefore);

        return new WalOperationResult(lsnBefore, lsnAfter, bytesGenerated);
    }

    /**
     * Tracka operaciju koja vraća rezultat.
     */
    public <T> TrackedResult<T> trackWithResult(java.util.function.Supplier<T> operation) {
        String lsnBefore = walRepository.getCurrentWalInsertLsn();

        T result = operation.get();

        String lsnAfter = walRepository.getCurrentWalInsertLsn();
        Long bytesGenerated = walRepository.calculateWalBytesGenerated(lsnAfter, lsnBefore);

        WalOperationResult walResult = new WalOperationResult(lsnBefore, lsnAfter, bytesGenerated);
        return new TrackedResult<>(result, walResult);
    }

    /**
     * Rezultat operacije sa WAL tracking informacijama.
     */
    public record TrackedResult<T>(T result, WalOperationResult walTracking) {}
}

