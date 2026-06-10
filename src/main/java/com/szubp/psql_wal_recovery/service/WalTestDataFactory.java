package com.szubp.psql_wal_recovery.service;

import com.szubp.psql_wal_recovery.db.model.*;
import com.szubp.psql_wal_recovery.model.XbrlDocumentType;
import com.szubp.psql_wal_recovery.repository.AuthUserRepository;
import com.szubp.psql_wal_recovery.repository.XbrlReportSuiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Factory za kreiranje test podataka za WAL demonstracije.
 */
@Component
public class WalTestDataFactory {

    @Autowired
    private AuthUserRepository userRepository;

    @Autowired
    private XbrlReportSuiteRepository suiteRepository;

    private static final String TEST_USER_EMAIL = "andrea.valcic@elfak.rs";

    /**
     * Pronalazi ili kreira test korisnika.
     */
    public AuthUser getOrCreateTestUser() {
        Optional<AuthUser> existing = userRepository.findByEmail(TEST_USER_EMAIL);

        if (existing.isPresent()) {
            return existing.get();
        }

        AuthUser user = new AuthUser();
        user.setEmail(TEST_USER_EMAIL);
        user.setFullname("Andrea Valcic");
        user.setKvknumber("80841716");
        return userRepository.save(user);
    }

    /**
     * Pronalazi ili kreira test suite za datog korisnika.
     */
    public XbrlReportSuite getOrCreateTestSuite(AuthUser user) {
        List<XbrlReportSuite> suites = suiteRepository
            .findAllByKvknumberOrderByCreatedAtDesc(user.getKvknumber());

        if (!suites.isEmpty()) {
            return suites.get(0);
        }

        return createNewTestSuite(user, "WAL_TEST_SUITE");
    }

    /**
     * Kreira novu test suite.
     */
    public XbrlReportSuite createNewTestSuite(AuthUser user, String namePrefix) {
        XbrlReportSuite suite = new XbrlReportSuite();
        suite.setKvknumber(user.getKvknumber());
        suite.setName(namePrefix + "_" + System.currentTimeMillis());
        suite.setEntryPoint("https://taxonomy.example/test.xsd");
        suite.setXbrl("<xbrl><test>Initial data</test></xbrl>");
        suite.setStatus(XbrlDocumentStatus.ACTIVE);
        suite.setType(XbrlDocumentType.DVI);
        suite.setDataYear(2024);
        suite.setCreatedAt(new Date());
        suite.setCreatedBy(user);
        suite.setModifiedBy(user);
        suite.setCompletedBy(user);
        return suite;
    }

    /**
     * Kreira test attachment.
     */
    public XbrlAttachment createTestAttachment(
        XbrlReportSuite suite,
        AuthUser user,
        String name,
        int sizeBytes,
        XbrlAttachmentType type
    ) {
        XbrlAttachment attachment = new XbrlAttachment();
        attachment.setName(name);
        attachment.setType(type);
        attachment.setMimeType("application/pdf");
        attachment.setSha1(generateSha1Hash(name));
        attachment.setFile(generateTestFileContent(sizeBytes));
        attachment.setSize((long) sizeBytes);
        attachment.setSuite(suite);
        attachment.setCreatedAt(new Date());
        attachment.setCreatedBy(user);
        return attachment;
    }

    /**
     * Generiše veliki XBRL sadržaj za testiranje.
     */
    public String generateLargeXbrl(int elementCount) {
        StringBuilder sb = new StringBuilder("<xbrl>");
        for (int i = 0; i < elementCount; i++) {
            sb.append("<element").append(i).append(">")
              .append("Data ").append(i)
              .append("</element").append(i).append(">");
        }
        sb.append("</xbrl>");
        return sb.toString();
    }

    /**
     * Generiše test file sadržaj.
     */
    public byte[] generateTestFileContent(int sizeBytes) {
        byte[] content = new byte[sizeBytes];
        new Random().nextBytes(content);
        return content;
    }

    /**
     * Generiše mock SHA1 hash.
     */
    private String generateSha1Hash(String input) {
        return String.format("%040x", new Random().nextLong());
    }
}

