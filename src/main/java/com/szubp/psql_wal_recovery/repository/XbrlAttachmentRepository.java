package com.szubp.psql_wal_recovery.repository;

import com.szubp.psql_wal_recovery.db.model.XbrlAttachment;
import com.szubp.psql_wal_recovery.db.model.XbrlAttachmentType;
import com.szubp.psql_wal_recovery.db.model.XbrlReportSuite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface XbrlAttachmentRepository extends JpaRepository<XbrlAttachment, String> {

	List<XbrlAttachment> findBySuite(XbrlReportSuite suite);

	boolean existsBySuiteAndType(XbrlReportSuite suite, XbrlAttachmentType type);

	Optional<XbrlAttachment> findByType(XbrlAttachmentType type);

	Optional<XbrlAttachment> findBySuiteIdAndType(String suiteId, XbrlAttachmentType type);
}
