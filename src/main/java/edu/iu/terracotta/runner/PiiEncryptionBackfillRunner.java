package edu.iu.terracotta.runner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import edu.iu.terracotta.security.pii.PiiCipher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Encrypts lti_user emails and display names stored before those columns were encrypted, and
 * fills in missing email hashes. It runs in the background after startup, a batch at a time, and
 * only touches rows that still need it, so it's a no-op once everything is converted.
 *
 * Until a row is converted it still works: the converter reads plain text as-is. Its email
 * can't be matched by hash yet, though, so messaging may miss that user until the backfill
 * reaches it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"PMD.GuardLogStatement"})
public class PiiEncryptionBackfillRunner implements ApplicationListener<ApplicationReadyEvent> {

    private static final String NEEDS_BACKFILL = """
        SELECT user_id, email, displayname
        FROM lti_user
        WHERE user_id > ?
            AND (
                (email IS NOT NULL AND email NOT LIKE 'pii1:%')
                OR (displayname IS NOT NULL AND displayname NOT LIKE 'pii1:%')
                OR (email IS NOT NULL AND email_hash IS NULL)
            )
        ORDER BY user_id
        LIMIT ?
        """;

    // only applies if the row hasn't changed since it was read, so a concurrent edit isn't lost
    private static final String UPDATE = "UPDATE lti_user SET email = ?, email_hash = ?, displayname = ? WHERE user_id = ? AND %s AND %s";

    private final JdbcTemplate jdbcTemplate;
    private final PiiCipher piiCipher;

    @Value("${app.pii.encryption.backfill.enabled:true}")
    private boolean enabled;

    @Value("${app.pii.encryption.backfill.batch-size:500}")
    private int batchSize;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!enabled) {
            return;
        }

        Thread thread = new Thread(this::backfill, "pii-encryption-backfill");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * @return the number of rows converted
     */
    public int backfill() {
        int converted = 0;
        int skipped = 0;
        long lastUserId = 0;

        try {
            while (true) {
                List<Row> rows = jdbcTemplate.query(
                    NEEDS_BACKFILL,
                    (rs, rowNum) -> new Row(rs.getLong("user_id"), rs.getString("email"), rs.getString("displayname")),
                    lastUserId,
                    batchSize
                );

                if (rows.isEmpty()) {
                    break;
                }

                for (Row row : rows) {
                    lastUserId = row.userId();

                    if (convert(row)) {
                        converted++;
                    } else {
                        skipped++;
                    }
                }
            }
        } catch (RuntimeException e) {
            log.error("Encrypting LTI user PII stopped after converting [{}] users; it resumes on the next startup.", converted, e);
            return converted;
        }

        if (converted > 0 || skipped > 0) {
            // a skipped row changed while it was being converted; the app saved it encrypted
            log.info("Encrypted the email and display name of [{}] LTI users ([{}] changed meanwhile and were left as saved).", converted, skipped);
        }

        return converted;
    }

    private boolean convert(Row row) {
        String plainEmail = piiCipher.decrypt(row.email());
        String email = PiiCipher.isEncrypted(row.email()) ? row.email() : piiCipher.encrypt(row.email());
        String displayName = PiiCipher.isEncrypted(row.displayName()) ? row.displayName() : piiCipher.encrypt(row.displayName());

        List<Object> args = new ArrayList<>(List.of(row.userId()));
        String sql = String.format(UPDATE, unchanged("email", row.email(), args), unchanged("displayname", row.displayName(), args));
        args.addAll(0, Arrays.asList(email, piiCipher.hashEmail(plainEmail), displayName));

        return jdbcTemplate.update(sql, args.toArray()) == 1;
    }

    private static String unchanged(String column, String readValue, List<Object> args) {
        if (readValue == null) {
            return column + " IS NULL";
        }

        args.add(readValue);

        return column + " = ?";
    }

    private record Row(long userId, String email, String displayName) {}

}
