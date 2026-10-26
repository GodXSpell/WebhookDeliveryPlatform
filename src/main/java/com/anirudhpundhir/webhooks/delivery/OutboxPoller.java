package com.anirudhpundhir.webhooks.delivery;

import com.anirudhpundhir.webhooks.config.WebhookProperties;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads pending outbox work without making an HTTP request yet.
 *
 * <p>The row locks make this poll safe to run in multiple application
 * instances. The next stage will replace the log statement with delivery
 * claiming and sending.
 */
@Component
@ConditionalOnProperty(name = "webhooks.poller-enabled", havingValue = "true", matchIfMissing = true)
public class OutboxPoller {
    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);

    private final JdbcTemplate jdbc;
    private final WebhookProperties properties;

    public OutboxPoller(JdbcTemplate jdbc, WebhookProperties properties) {
        this.jdbc = jdbc;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${webhooks.poll-interval:1s}")
    @Transactional
    public void poll() {
        List<PendingEvent> pending = jdbc.query(
                """
                SELECT o.id, o.event_id, e.type, o.created_at
                FROM outbox o
                JOIN events e ON e.id = o.event_id
                WHERE o.status = 'PENDING'
                ORDER BY o.created_at
                FOR UPDATE OF o SKIP LOCKED
                LIMIT ?
                """,
                (rs, row) -> new PendingEvent(
                        rs.getObject("id", UUID.class),
                        rs.getObject("event_id", UUID.class),
                        rs.getString("type"),
                        rs.getTimestamp("created_at").toInstant()),
                properties.pollBatchSize());
        if (!pending.isEmpty()) {
            log.info("Found {} pending outbox event(s): {}", pending.size(),
                    pending.stream().map(PendingEvent::eventId).toList());
        }
    }

    record PendingEvent(UUID outboxId, UUID eventId, String type, Instant createdAt) {}
}
