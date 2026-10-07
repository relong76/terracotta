package edu.iu.terracotta.runner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import edu.iu.terracotta.security.pii.PiiCipher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Encrypts personal values (and LMS OAuth tokens) stored before their columns were encrypted, and fills in missing
 * email hashes. It runs in the background after startup, a batch at a time, and only touches rows
 * that still need it, so it's a no-op once everything is converted.
 *
 * Until a row is converted it still works: the converter reads plain text as-is. A user whose
 * email isn't hashed yet can't be matched by email, though, so messaging may miss them until the
 * backfill reaches their row.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings({"PMD.GuardLogStatement"})
public class PiiEncryptionBackfillRunner implements ApplicationListener<ApplicationReadyEvent> {

    /**
     * The encrypted columns, by table. lms_user_batch isn't here: it only holds a roster while a
     * sync runs, and the migration dropped what was left of it.
     */
    static final List<Target> TARGETS = List.of(
        new Target("lti_user", "user_id", List.of("email", "displayname"), "email", "email_hash"),
        new Target("api_token", "token_id", List.of("lms_user_name", "access_token", "refresh_token"), null, null),
        new Target("terr_messaging_email_reply_to", "id", List.of("email"), null, null),
        new Target("terr_submission_comment", "submission_comment_id", List.of("creator"), null, null),
        new Target("terr_question_submission_comment", "question_submission_comment_id", List.of("creator"), null, null),
        new Target("terr_messaging_piped_text_item_value", "id", List.of("value"), null, null),
        new Target("terr_messaging_message_log", "id", List.of("body"), null, null)
    );

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
     * @return the number of rows converted, across all tables
     */
    public int backfill() {
        int converted = 0;

        for (Target target : TARGETS) {
            try {
                converted += backfill(target);
            } catch (RuntimeException e) {
                // the rest of the tables are still worth converting; this one resumes on the next startup
                log.error("Encrypting the personal data in table [{}] stopped; it resumes on the next startup.", target.table(), e);
            }
        }

        return converted;
    }

    private int backfill(Target target) {
        int converted = 0;
        int skipped = 0;
        long lastId = 0;

        while (true) {
            List<Map<String, String>> rows = new ArrayList<>();
            List<Long> ids = new ArrayList<>();

            jdbcTemplate.query(
                target.selectSql(),
                rs -> {
                    ids.add(rs.getLong(target.idColumn()));
                    rows.add(values(rs, target));
                },
                lastId,
                batchSize
            );

            if (rows.isEmpty()) {
                break;
            }

            for (int i = 0; i < rows.size(); i++) {
                lastId = ids.get(i);

                if (convert(target, lastId, rows.get(i))) {
                    converted++;
                } else {
                    skipped++;
                }
            }
        }

        if (converted > 0 || skipped > 0) {
            // a skipped row changed while it was being converted; the app saved it encrypted
            log.info("Encrypted the personal data of [{}] rows in table [{}] ([{}] changed meanwhile and were left as saved).", converted, target.table(), skipped);
        }

        return converted;
    }

    private boolean convert(Target target, long id, Map<String, String> read) {
        List<String> sets = new ArrayList<>();
        List<Object> setArgs = new ArrayList<>();

        for (String column : target.columns()) {
            sets.add(column + " = ?");
            setArgs.add(PiiCipher.isEncrypted(read.get(column)) ? read.get(column) : piiCipher.encrypt(read.get(column)));
        }

        if (target.hashColumn() != null) {
            sets.add(target.hashColumn() + " = ?");
            setArgs.add(piiCipher.hashEmail(piiCipher.decrypt(read.get(target.hashSource()))));
        }

        // only applies if the row hasn't changed since it was read, so a concurrent edit isn't lost
        List<String> unchanged = new ArrayList<>();
        List<Object> whereArgs = new ArrayList<>(List.of(id));

        for (String column : target.columns()) {
            if (read.get(column) == null) {
                unchanged.add(column + " IS NULL");
            } else {
                unchanged.add(column + " = ?");
                whereArgs.add(read.get(column));
            }
        }

        String sql = "UPDATE " + target.table() + " SET " + String.join(", ", sets)
            + " WHERE " + target.idColumn() + " = ? AND " + String.join(" AND ", unchanged);
        setArgs.addAll(whereArgs);

        return jdbcTemplate.update(sql, setArgs.toArray()) == 1;
    }

    private static Map<String, String> values(ResultSet rs, Target target) throws SQLException {
        Map<String, String> values = new LinkedHashMap<>();

        for (String column : target.columns()) {
            values.put(column, rs.getString(column));
        }

        return values;
    }

    /**
     * A table's encrypted columns, and optionally the hash column kept for one of them.
     */
    record Target(String table, String idColumn, List<String> columns, String hashSource, String hashColumn) {

        // rows with a value not yet encrypted, or an email not yet hashed
        String selectSql() {
            String needsEncrypting = columns.stream()
                .map(column -> "(" + column + " IS NOT NULL AND " + column + " NOT LIKE '" + PiiCipher.PREFIX + "%')")
                .collect(Collectors.joining(" OR "));

            if (hashColumn != null) {
                needsEncrypting += " OR (" + hashSource + " IS NOT NULL AND " + hashColumn + " IS NULL)";
            }

            return "SELECT " + idColumn + ", " + String.join(", ", columns)
                + " FROM " + table
                + " WHERE " + idColumn + " > ? AND (" + needsEncrypting + ")"
                + " ORDER BY " + idColumn
                + " LIMIT ?";
        }

    }

}
