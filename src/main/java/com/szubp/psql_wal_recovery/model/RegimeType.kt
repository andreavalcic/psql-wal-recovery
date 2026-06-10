package com.szubp.psql_wal_recovery.model

import com.szubp.psql_wal_recovery.model.XbrlDocumentType.DVI
import com.szubp.psql_wal_recovery.model.XbrlDocumentType.KVK

enum class RegimeType(val documentType: XbrlDocumentType, val value: String, val code: String, val allowedForDviDatavaultConnection: Boolean = true) {
	//DVI
	ADMINISTRATIEVE_SCHEIDING(DVI, "administratieve-scheiding", "ADM-ENK"),
	ADMINISTRATIEVE_SCHEIDING_GECONSOLIDEERD(DVI, "administratieve-scheiding-geconsolideerd", "ADM-CON"),
	VERLICHT_REGIME(DVI, "verlicht-regime", "VER-ENK"),
	VERLICHT_REGIME_GECONSOLIDEERD(DVI, "verlicht-regime-geconsolideerd", "VER-CON"),
	HYBRIDE_SCHEIDING(DVI, "hybride-scheiding", "HYB"),
	JURIDISCHE_SCHEIDING(DVI, "juridische-scheiding", "JUR"),
	//KVK
	GOVAUTH_REALESTATE(KVK, "nlgaap-toegelaten-instellingen-volkshuisvesting", "NLGAAP-REALESTATE"),
	GOVAUTH_REALESTATE_SMALL(KVK, "nlgaap-klein", "NLGAAP-SMALL", false),
}
