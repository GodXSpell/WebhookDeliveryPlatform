package com.anirudhpundhir.webhooks.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class EventPublisherTest {
    @Mock JdbcTemplate jdbc;

    @Test
    void writesEventAndOutboxRow() {
        when(jdbc.update(anyString(), any(), any(), any(), any())).thenReturn(1);
        EventPublisher.Published published =
                new EventPublisher(jdbc, new ObjectMapper()).publish("payment.success",
                        JsonNodeFactory.instance.objectNode().put("amount", 100));

        assertThat(published.eventId()).isNotNull();
        assertThat(published.outboxId()).isNotNull();
        verify(jdbc).update(startsWith("INSERT INTO events"), any(), any(), any(), any());
        verify(jdbc).update(startsWith("INSERT INTO outbox"), any(), any(), any(), any());
    }
}
