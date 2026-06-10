package com.szubp.psql_wal_recovery.dto;

/**
 * Rezultat WAL operacije sa LSN pozicijama i generisanim bajtovima.
 */
public record WalOperationResult(
    String lsnBefore,
    String lsnAfter,
    Long walBytesGenerated
) {
    public String getWalSizePretty() {
        if (walBytesGenerated == null) return "0 bytes";
        if (walBytesGenerated < 1024) return walBytesGenerated + " bytes";
        if (walBytesGenerated < 1024 * 1024) return (walBytesGenerated / 1024) + " KB";
        return (walBytesGenerated / (1024 * 1024)) + " MB";
    }
}

