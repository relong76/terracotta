package edu.iu.terracotta.security.pii;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import edu.iu.Terracotta;
import edu.iu.terracotta.connectors.generic.dao.entity.lms.LmsUserBatch;
import edu.iu.terracotta.connectors.generic.dao.entity.lms.LmsUserBatchEmailProjection;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.LtiUserEntity;
import edu.iu.terracotta.connectors.generic.dao.entity.lti.PlatformDeployment;
import edu.iu.terracotta.connectors.generic.dao.model.enums.LmsConnector;
import edu.iu.terracotta.connectors.generic.dao.repository.lms.LmsUserBatchRepository;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.LtiUserRepository;
import edu.iu.terracotta.connectors.generic.dao.repository.lti.PlatformDeploymentRepository;
import edu.iu.terracotta.runner.PiiEncryptionBackfillRunner;

/**
 * Runs the real Hibernate mapping against H2 to check what lands in the tables: personal values
 * are stored encrypted, read back decrypted, and email hashes follow their emails. Also checks the
 * startup backfill converts rows saved before encryption.
 */
@SpringBootTest(
    classes = Terracotta.class,
    properties = {
        "aws.enabled=false",
        // isolated in-memory H2 instance, overriding any ambient/profile-based datasource
        "spring.datasource.url=jdbc:h2:mem:pii-encryption-it;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=sa",
        "spring.liquibase.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        // the tests run the backfill themselves; at startup it would race them
        "app.pii.encryption.backfill.enabled=false"
    }
)
@ActiveProfiles("test")
class PiiEncryptionRealHibernateTest {

    @Autowired private LtiUserRepository ltiUserRepository;
    @Autowired private LmsUserBatchRepository lmsUserBatchRepository;
    @Autowired private PlatformDeploymentRepository platformDeploymentRepository;
    @Autowired private PiiCipher piiCipher;
    @Autowired private PiiEncryptionBackfillRunner backfillRunner;
    @Autowired private JdbcTemplate jdbcTemplate;

    private PlatformDeployment platformDeployment;

    @BeforeEach
    void seed() {
        jdbcTemplate.update("DELETE FROM lti_user");
        jdbcTemplate.update("DELETE FROM lms_user_batch");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update("DELETE FROM terr_submission_comment");
        jdbcTemplate.update("DELETE FROM terr_messaging_message_log");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        platformDeployment = platformDeploymentRepository.saveAndFlush(
            PlatformDeployment.builder()
                .iss("https://issuer.example.org")
                .clientId(UUID.randomUUID().toString())
                .oidcEndpoint("https://issuer.example.org/oidc")
                .enableAutomaticDeployments(false)
                .lmsConnector(LmsConnector.CANVAS)
                .build()
        );
    }

    @Test
    void storesTheEmailAndNameEncrypted() {
        LtiUserEntity saved = save("ada@example.com", "Ada Lovelace");

        Map<String, Object> row = rawRow(saved.getUserId());

        assertTrue(PiiCipher.isEncrypted((String) row.get("email")));
        assertFalse(((String) row.get("email")).contains("ada"));
        assertTrue(PiiCipher.isEncrypted((String) row.get("displayname")));
        assertFalse(((String) row.get("displayname")).contains("Ada"));
        assertEquals(piiCipher.hashEmail("ada@example.com"), row.get("email_hash"));
    }

    @Test
    void readsTheEmailAndNameBackDecrypted() {
        long userId = save("ada@example.com", "Ada Lovelace").getUserId();

        LtiUserEntity loaded = ltiUserRepository.findById(userId).orElseThrow();

        assertEquals("ada@example.com", loaded.getEmail());
        assertEquals("Ada Lovelace", loaded.getDisplayName());
    }

    @Test
    void findsUsersByEmailHashIgnoringCase() {
        long userId = save("ada@example.com", "Ada Lovelace").getUserId();
        save("grace@example.com", "Grace Hopper");

        List<LtiUserEntity> found = ltiUserRepository.findAllByEmailHashInAndPlatformDeployment_KeyId(
            List.of(piiCipher.hashEmail("ADA@example.com")),
            platformDeployment.getKeyId()
        );

        assertEquals(List.of(userId), found.stream().map(LtiUserEntity::getUserId).toList());
    }

    @Test
    void updatesTheHashWhenTheEmailChanges() {
        long userId = save("ada@example.com", "Ada Lovelace").getUserId();

        LtiUserEntity loaded = ltiUserRepository.findById(userId).orElseThrow();
        loaded.setEmail("countess@example.com");
        ltiUserRepository.saveAndFlush(loaded);

        assertEquals(piiCipher.hashEmail("countess@example.com"), rawRow(userId).get("email_hash"));
    }

    @Test
    void clearsTheHashWhenTheEmailIsRemoved() {
        long userId = save("ada@example.com", "Ada Lovelace").getUserId();

        LtiUserEntity loaded = ltiUserRepository.findById(userId).orElseThrow();
        loaded.setEmail(null);
        ltiUserRepository.saveAndFlush(loaded);

        assertNull(rawRow(userId).get("email_hash"));
    }

    @Test
    void backfillEncryptsRowsSavedBeforeEncryption() {
        long legacy = insertRaw("ada@example.com", "Ada Lovelace", null);
        // encrypted already, but saved before its hash was filled in
        long missingHash = insertRaw(piiCipher.encrypt("grace@example.com"), piiCipher.encrypt("Grace Hopper"), null);
        long noPii = insertRaw(null, null, null);

        assertEquals(2, backfillRunner.backfill());

        Map<String, Object> legacyRow = rawRow(legacy);
        assertTrue(PiiCipher.isEncrypted((String) legacyRow.get("email")));
        assertTrue(PiiCipher.isEncrypted((String) legacyRow.get("displayname")));
        assertEquals(piiCipher.hashEmail("ada@example.com"), legacyRow.get("email_hash"));
        assertEquals("Ada Lovelace", ltiUserRepository.findById(legacy).orElseThrow().getDisplayName());

        assertEquals(piiCipher.hashEmail("grace@example.com"), rawRow(missingHash).get("email_hash"));
        assertEquals("Grace Hopper", ltiUserRepository.findById(missingHash).orElseThrow().getDisplayName());

        assertNull(rawRow(noPii).get("email"));
    }

    @Test
    void backfillIsANoOpOnceEverythingIsConverted() {
        insertRaw("ada@example.com", "Ada Lovelace", null);
        backfillRunner.backfill();

        assertEquals(0, backfillRunner.backfill());
    }

    @Test
    void readsALegacyRowBeforeTheBackfillReachesIt() {
        long legacy = insertRaw("ada@example.com", "Ada Lovelace", null);

        LtiUserEntity loaded = ltiUserRepository.findById(legacy).orElseThrow();

        assertEquals("ada@example.com", loaded.getEmail());
        assertEquals("Ada Lovelace", loaded.getDisplayName());
    }

    @Test
    void storesAStagedRosterUserEncryptedAndFindsThemByEmailHash() {
        UUID batchId = UUID.randomUUID();
        LmsUserBatch staged = lmsUserBatchRepository.saveAndFlush(
            LmsUserBatch.builder().batchId(batchId).userKey("key-1").lmsUserId("lms-1").email("Ada@Example.com").name("Ada Lovelace").build()
        );

        Map<String, Object> row = jdbcTemplate.queryForMap("SELECT email, email_hash, name FROM lms_user_batch WHERE id = ?", staged.getId());
        assertTrue(PiiCipher.isEncrypted((String) row.get("email")));
        assertTrue(PiiCipher.isEncrypted((String) row.get("name")));
        assertEquals(piiCipher.hashEmail("ada@example.com"), row.get("email_hash"));

        List<LmsUserBatchEmailProjection> found = lmsUserBatchRepository.findBatchProjectionsByBatchIdAndEmailHashIn(
            batchId,
            List.of(piiCipher.hashEmail("ada@example.com")),
            PageRequest.of(0, 10)
        );

        // the query's projection is decrypted too, not just whole entities
        assertEquals(1, found.size());
        assertEquals("Ada@Example.com", found.get(0).getEmail());
        assertEquals("lms-1", found.get(0).getLmsUserId());
    }

    @Test
    void backfillEncryptsTheOtherTablesToo() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.update(
            "INSERT INTO terr_submission_comment (submission_comment_id, submission_submission_id, creator, comment, uuid, entity_version) VALUES (1, 1, ?, 'Nice work', ?, 0)",
            "Ada Lovelace", UUID.randomUUID()
        );
        jdbcTemplate.update(
            "INSERT INTO terr_messaging_message_log (id, lti_user_user_id, message_id, body, uuid, entity_version) VALUES (1, 1, 1, ?, ?, 0)",
            "<p>Hi Ada, here are your results.</p>", UUID.randomUUID()
        );
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        assertEquals(2, backfillRunner.backfill());

        String creator = jdbcTemplate.queryForObject("SELECT creator FROM terr_submission_comment WHERE submission_comment_id = 1", String.class);
        String body = jdbcTemplate.queryForObject("SELECT body FROM terr_messaging_message_log WHERE id = 1", String.class);

        assertEquals("Ada Lovelace", piiCipher.decrypt(creator));
        assertTrue(PiiCipher.isEncrypted(creator));
        assertEquals("<p>Hi Ada, here are your results.</p>", piiCipher.decrypt(body));
        assertTrue(PiiCipher.isEncrypted(body));
        // the comment itself isn't personal data and stays as written
        assertEquals("Nice work", jdbcTemplate.queryForObject("SELECT comment FROM terr_submission_comment WHERE submission_comment_id = 1", String.class));
    }

    private LtiUserEntity save(String email, String displayName) {
        LtiUserEntity ltiUserEntity = new LtiUserEntity(UUID.randomUUID().toString(), null, platformDeployment);
        ltiUserEntity.setEmail(email);
        ltiUserEntity.setDisplayName(displayName);

        return ltiUserRepository.saveAndFlush(ltiUserEntity);
    }

    // a row written straight to the table, as it was before these columns were encrypted
    private long insertRaw(String email, String displayName, String emailHash) {
        jdbcTemplate.update(
            "INSERT INTO lti_user (user_key, key_id, email, email_hash, displayname, uuid, entity_version) VALUES (?, ?, ?, ?, ?, ?, 0)",
            UUID.randomUUID().toString(),
            platformDeployment.getKeyId(),
            email,
            emailHash,
            displayName,
            UUID.randomUUID()
        );

        return jdbcTemplate.queryForObject("SELECT MAX(user_id) FROM lti_user", Long.class);
    }

    private Map<String, Object> rawRow(long userId) {
        return jdbcTemplate.queryForMap("SELECT email, email_hash, displayname FROM lti_user WHERE user_id = ?", userId);
    }

}
